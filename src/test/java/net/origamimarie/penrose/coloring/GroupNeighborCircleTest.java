package net.origamimarie.penrose.coloring;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
public class GroupNeighborCircleTest {

  private static Random random = new Random();

  @Test
  public void foo() {
    List<Boolean> booleans;
    boolean result;

    booleans = expandBooleanList(Arrays.asList(true));
    result = GroupNeighborCircle.isBridge(booleans);
    Assert.assertFalse(result);

    booleans = expandBooleanList(Arrays.asList(true, true));
    result = GroupNeighborCircle.isBridge(booleans);
    Assert.assertFalse(result);

    booleans = expandBooleanList(Arrays.asList(true, true, true));
    result = GroupNeighborCircle.isBridge(booleans);
    Assert.assertFalse(result);

    booleans = expandBooleanList(Arrays.asList(true, true, true, true));
    result = GroupNeighborCircle.isBridge(booleans);
    Assert.assertFalse(result);

    booleans = expandBooleanList(Arrays.asList(false, true, true, true));
    result = GroupNeighborCircle.isBridge(booleans);
    Assert.assertFalse(result);

    booleans = expandBooleanList(Arrays.asList(true, false, true, true));
    result = GroupNeighborCircle.isBridge(booleans);
    Assert.assertFalse(result);

    booleans = expandBooleanList(Arrays.asList(true, true, true, false));
    result = GroupNeighborCircle.isBridge(booleans);
    Assert.assertFalse(result);

    booleans = expandBooleanList(Arrays.asList(false, false, true, true));
    result = GroupNeighborCircle.isBridge(booleans);
    Assert.assertFalse(result);

    booleans = expandBooleanList(Arrays.asList(false, true, true, false));
    result = GroupNeighborCircle.isBridge(booleans);
    Assert.assertFalse(result);

    booleans = expandBooleanList(Arrays.asList(true, false, true, false));
    result = GroupNeighborCircle.isBridge(booleans);
    Assert.assertTrue(result);

  }

  private static List<Boolean> expandBooleanList(List<Boolean> list) {
    List<Boolean> result = new ArrayList<>(list.size());
    for(boolean b : list) {
      for(int i = 0; i < random.nextInt(5)+1; i++) {
        result.add(b);
      }
    }
    return result;
  }

}
