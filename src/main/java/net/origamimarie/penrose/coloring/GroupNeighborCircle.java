package net.origamimarie.penrose.coloring;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class GroupNeighborCircle {

  // Track them in order.
  private List<ShapeGroup> neighborsList;
  // Quick look-up.
  private Set<ShapeGroup> neighborsSet;

  public GroupNeighborCircle() {
    neighborsList = new ArrayList<>();
    neighborsSet = new HashSet<>();
  }

  public void addNeighborInOrder(ShapeGroup group) {
    if(neighborsSet.add(group)) {
      neighborsList.add(group);
    }
  }

  public void removeNeighbor(ShapeGroup group) {
    neighborsList.remove(group);
    neighborsSet.remove(group);
  }

  public Set<ShapeGroup> getNeighborsSetCopy() {
    return new HashSet<>(neighborsSet);
  }

  public List<ShapeGroup> getNeighborsListCopy() {
    return new ArrayList<>(neighborsList);
  }

  public boolean contains(ShapeGroup group) {
    return neighborsSet.contains(group);
  }

  // Are there two distinct null patches of neighbors separated by non-null patches?
  // If so, then we are a bridge and one of them might be an island.
  private boolean isBridge() {
    if(neighborsList.size() == 0) {
      return false;
    }
    List<Boolean> cellIsNull = new ArrayList<>(neighborsList.size());
    for(ShapeGroup group : neighborsList) {
      cellIsNull.add(group.getColoredShapeGroup().getColor() == null);
    }
    return isBridge(cellIsNull);
  }

  protected static boolean isBridge(List<Boolean> cellIsNull) {
    boolean onNullPatch = cellIsNull.get(0);
    boolean startsNull = onNullPatch;
    int emptyPatches = startsNull ? 1 : 0;
    for(boolean currentNull : cellIsNull) {
      emptyPatches += currentNull && !onNullPatch ? 1 : 0;
      onNullPatch = currentNull;
      if(!startsNull && emptyPatches == 2 || emptyPatches == 3) {
        return true;
      }
    }
    return emptyPatches == 2 && !onNullPatch;
  }

  // If we're a bridge, there may be an island adjacent.
  // An island consists of an isolated cluster of null-colored shapes
  // that are at most 3 hops from each other.
  // Anything larger is too big to worry about.
  public Set<ShapeGroup> getNeighboringIsland() {
    if(!isBridge()) {
      return null;
    }
    Set<ShapeGroup> triedNeighbors = new HashSet<>();
    for(int i = 0; i < neighborsList.size(); i++) {
      // Each search for an island can include more than one neighbor,
      // so make sure we aren't redoing this neighbor.
      if(neighborsList.get(i).getColoredShapeGroup().getColor() == null &&
              !triedNeighbors.contains(neighborsList.get(i))) {
        Set<ShapeGroup> islandSeedNeighbors = getNullColoredNeighborsNearIndex(i);
        triedNeighbors.addAll(islandSeedNeighbors);
        Set<ShapeGroup> proposedIsland = new HashSet<>();
        // Get everybody within the bounds of the proposed island.
        // This can go a few hops out.
        Set<ShapeGroup> thisHopSeeds = new HashSet<>(islandSeedNeighbors);
        Set<ShapeGroup> nextHopSeeds = new HashSet<>();
        for(int hops = 0; hops < 3; hops++) {
          // This dodges an interesting, strange concurrent modification exception.
          for(ShapeGroup seed : new ArrayList<>(thisHopSeeds)) {
            proposedIsland.add(seed);
            Set<ShapeGroup> newNeighbors = seed.getNeighbors();
            for(ShapeGroup newSeed : newNeighbors) {
              if(newSeed.getColoredShapeGroup().getColor() == null) {
                proposedIsland.add(newSeed);
                nextHopSeeds.add(newSeed);
              }
            }
          }
          thisHopSeeds = nextHopSeeds;
        }
        for(ShapeGroup group : proposedIsland) {
          if(group.getColoredShapeGroup().getColor() != null) {
            log.debug("uh-oh");
          }
        }
        // Now to see if there are nulls connected that aren't within the island.
        // If there are, then we're not actually an island.
        boolean isIsland = true;
        for(ShapeGroup group : proposedIsland) {
          for(ShapeGroup neighbor : group.getNeighbors()) {
            if(neighbor.getColoredShapeGroup().getColor() == null && !proposedIsland.contains(neighbor)) {
              isIsland = false;
              break;
            }
          }
          if(!isIsland) {
            break;
          }
        }
        if(isIsland) {
          return proposedIsland;
        }
      }
    }
    return null;
  }



  // Just the items in the neighbors list that are proximal to this group.
  private Set<ShapeGroup> getNullColoredNeighborsNearIndex(int index) {
    int listSize = neighborsList.size();
    Set<ShapeGroup> nullNeighbors = new HashSet<>();
    for(int i = index; i < listSize; i++) {
      if(neighborsList.get(i).getColoredShapeGroup().getColor() == null) {
        nullNeighbors.add(neighborsList.get(i));
      } else {
        // Just until we get to the end of the null patch.
        break;
      }
    }
    // Walking backwards is a little harder.
    for(int i = index-1; i > 1-listSize; i--) {
      // Fun with remainders in java.
      int j = (i+listSize)%listSize;
      if(neighborsList.get(j).getColoredShapeGroup().getColor() == null) {
        nullNeighbors.add(neighborsList.get(j));
      } else {
        // Just until we get to the end of the null patch.
        break;
      }
    }
    return nullNeighbors;
  }

}
