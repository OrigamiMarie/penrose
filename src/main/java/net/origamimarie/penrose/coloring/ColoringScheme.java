package net.origamimarie.penrose.coloring;

import java.awt.Color;
import java.util.*;

public enum ColoringScheme {
  RAINBOW_32_FUZZY_ALTERNATING,
  RAINBOW_32_FUZZY_SIMILAR,
  RAINBOW_6,
  RAINBOW_5,
  CONFETTI;

  static {
    List<Color> colors32 = new ArrayList<>(32);
    for(int i = 0; i < 32; i++) {
      colors32.add(Color.getHSBColor((i * 8.0f) / 256.0f, 1, 1));
    }
    // RAINBOW_32_FUZZY_ALTERNATING initialization
    RAINBOW_32_FUZZY_ALTERNATING.colorsForPalette = new ArrayList<>();
    for(int i = 0; i < colors32.size(); i++) {
      Color color = colors32.get(i);
      List<Color> dissimilarColors = copyOutItems(colors32, i+6, 15);
      RAINBOW_32_FUZZY_ALTERNATING.colorsForPalette.add(new WhitelistColor(color, dissimilarColors));
    }
    // RAINBOW_32_FUZZY_SIMILAR initialization
    RAINBOW_32_FUZZY_SIMILAR.colorsForPalette = new ArrayList<>();
    for(int i = 0; i < colors32.size(); i++) {
      Color color = colors32.get(i);
      List<Color> similarColors = copyOutItems(colors32, i-6, 11);
      similarColors.remove(color);
      RAINBOW_32_FUZZY_SIMILAR.colorsForPalette.add(new WhitelistColor(color, similarColors));
    }
    // RAINBOW_6 initialization
    List<Color> colors6 = Arrays.asList(
            new Color(255, 0, 127),
            new Color(255, 127, 0),
            new Color(255, 255, 0),
            new Color(0, 191, 0),
            new Color(63, 63, 255),
            new Color(127, 0, 191));
    RAINBOW_6.colorsForPalette = new ArrayList<>();
    for(Color color : colors6) {
      RAINBOW_6.colorsForPalette.add(new BlacklistColor(color, Collections.singletonList(color)));
    }
    // RAINBOW_5 initialization
    List<Color> colors5 = Arrays.asList(
            Color.RED,
            Color.ORANGE,
            Color.GREEN,
            Color.BLUE,
            Color.MAGENTA);
    RAINBOW_5.colorsForPalette = new ArrayList<>();
    for(Color color : colors5) {
      RAINBOW_5.colorsForPalette.add(new BlacklistColor(color, Collections.singletonList(color)));
    }
    // CONFETTI initialization
    CONFETTI.colorsForPalette = new ArrayList<>();
    for(Color color : colors32) {
      CONFETTI.colorsForPalette.add(new WhitelistColor(color, Collections.singletonList(Color.BLACK)));
    }
    CONFETTI.colorsForPalette.add(new BlacklistColor(Color.BLACK, new ArrayList<Color>()));

    for(ColoringScheme scheme : ColoringScheme.values()) {
      scheme.justTheColors = new ArrayList<>();
      for(ColorForPalette paletteColor : scheme.colorsForPalette) {
        scheme.justTheColors.add(paletteColor.color);
      }
    }
  }

  public List<ColorForPalette> colorsForPalette;
  public List<Color> justTheColors;

  private static <T> List<T> copyOutItems(List<T> list, int start, int count) {
    List<T> result = new ArrayList<>();
    for(int i = 0; i < count; i++) {
      if(start < 0) {
        start = list.size() + start;
      }
      if(start > list.size() - 1) {
        start = start - (list.size() - 1);
      }
      result.add(list.get(start));
      start++;
    }
    return result;
  }

}
