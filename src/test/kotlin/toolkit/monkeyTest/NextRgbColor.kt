package toolkit.monkeyTest

import color.rgb.StandardRgbColor

fun nextStandardRgbColor(): StandardRgbColor {
    return StandardRgbColor(
        red = nextUnitInterval(),
        green = nextUnitInterval(),
        blue = nextUnitInterval(),
    )
}