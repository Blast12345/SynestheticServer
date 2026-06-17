package color

sealed interface ColorSpace

sealed interface RgbColorSpace : ColorSpace
data object StandardRGB : RgbColorSpace
data object LinearRGB : RgbColorSpace