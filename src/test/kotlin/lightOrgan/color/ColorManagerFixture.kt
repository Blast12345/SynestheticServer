package lightOrgan.color

import color.rgb.StandardRgbColor
import color.rgb.StandardRgbColors
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

data class ColorManagerFixture(
    val mock: ColorManager,
    val colorFlow: MutableStateFlow<StandardRgbColor>
) {

    companion object {
        fun create(): ColorManagerFixture {
            val fixture = ColorManagerFixture(
                mock = mockk<ColorManager>(),
                colorFlow = MutableStateFlow(StandardRgbColors.Black)
            )

            every { fixture.mock.color } returns fixture.colorFlow

            return fixture
        }
    }

}