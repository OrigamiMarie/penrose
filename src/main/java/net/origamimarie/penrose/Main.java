package net.origamimarie.penrose;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.File;
import java.io.IOException;

@Slf4j
public class Main {

  public static void main(String[] args) throws IOException {
    TilingGenerator generator = new TilingGenerator();
    File file = new File("/Users/mariep/personalcode/penrose/bar.svg");
    SvgOutput.pointListsToSvgFile(file, generator.getAllPointLists(), 20, null);

  }
}
