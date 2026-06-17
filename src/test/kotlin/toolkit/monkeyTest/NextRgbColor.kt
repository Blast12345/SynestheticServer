package toolkit.monkeyTest

import color.StandardRgbColor

fun nextStandardRgbColor(): StandardRgbColor {
    return StandardRgbColor(
        red = nextUnitInterval(),
        green = nextUnitInterval(),
        blue = nextUnitInterval(),
    )
}