package net.origamimarie.penrose;


import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Main {

  public static void main(String[] args) {
    TilingGenerator generator = new TilingGenerator();
    List<Point[]> pointLists = generator.getAllPointLists();
    log.debug("\n{}", SvgOutput.pointListsToSvg(pointLists, 200.0));
  }
}
