package net.origamimarie.penrose;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public class Main {

  public static void main(String[] args) throws IOException {
    TilingGenerator generator = new TilingGenerator();
    File file = new File("/Users/mariep/personalcode/penrose/bar.svg");
    SvgOutput.pointListsToSvgFile(file, generator.getAllPointLists(), 200, null);
    //log.debug(SvgOutput.pointListsToSvg(generator.getAllPointLists(), 200));

    Kite kite = new Kite();
    Dart dart = new Dart();
  }
}
