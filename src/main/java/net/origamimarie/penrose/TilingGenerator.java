package net.origamimarie.penrose;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Slf4j
public class TilingGenerator {

  Vertex liveVertex;

  public TilingGenerator() throws IOException {
    liveVertex = new Vertex();
    liveVertex.setLocation(new Point(0.0, 0.0));
/*
    liveVertex.addShape(new Kite(), Vwedge.N0, 0, 0);
    liveVertex = liveVertex.findAnyLiveNonFullVertex();
    liveVertex.addShape(new Dart(), Vwedge.D0, 2, 0);
    liveVertex = liveVertex.findAnyLiveNonFullVertex();
*/

    File file = new File("/Users/mariep/personalcode/penrose/foo.svg");

    for(int i = 0; i < 8; i++) {
      liveVertex.addRandomShape();
      liveVertex = liveVertex.findAnyLiveNonFullVertex();
      //SvgOutput.pointListsToSvgFile(file, getAllPointLists(), 20, null);
    }
  }

  public List<Point[]> getAllPointLists() {
    return liveVertex.getAllShapePoints();
  }

}
