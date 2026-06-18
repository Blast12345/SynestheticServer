package math.geometry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AngleTests {

    // Init
    @Test
    fun `must not be NaN`() {
        assertThrows<IllegalArgumentException> { Angle.fromRadians(Double.NaN) }
        assertThrows<IllegalArgumentException> { Angle.fromDegrees(Double.NaN) }
        assertThrows<IllegalArgumentException> { Angle.fromTurns(Double.NaN) }
    }

    @Test
    fun `must be finite`() {
        assertThrows<IllegalArgumentException> { Angle.fromRadians(Double.POSITIVE_INFINITY) }
        assertThrows<IllegalArgumentException> { Angle.fromDegrees(Double.POSITIVE_INFINITY) }
        assertThrows<IllegalArgumentException> { Angle.fromTurns(Double.POSITIVE_INFINITY) }
    }

    @Test
    fun `must not be negative infinity`() {
        assertThrows<IllegalArgumentException> { Angle.fromRadians(Double.NEGATIVE_INFINITY) }
        assertThrows<IllegalArgumentException> { Angle.fromDegrees(Double.NEGATIVE_INFINITY) }
        assertThrows<IllegalArgumentException> { Angle.fromTurns(Double.NEGATIVE_INFINITY) }
    }

    // Degrees -> X
    @Test
    fun `convert from degrees to radians`() {
        val angle = Angle.fromDegrees(180.0)
        assertEquals(Math.PI, angle.radians, 0.001)
    }

    @Test
    fun `convert from degrees to turns`() {
        val angle = Angle.fromDegrees(90.0)
        assertEquals(0.25, angle.turns, 0.001)
    }

    // Radians -> X
    @Test
    fun `convert from radians to degrees`() {
        val angle = Angle.fromRadians(Math.PI)
        assertEquals(180.0, angle.degrees, 0.001)
    }

    @Test
    fun `convert from radians to turns`() {
        val angle = Angle.fromRadians(2 * Math.PI)
        assertEquals(1.0, angle.turns, 0.001)
    }

    // Turns -> X
    @Test
    fun `convert from turns to radians`() {
        val angle = Angle.fromTurns(0.5)
        assertEquals(Math.PI, angle.radians, 0.001)
    }

    @Test
    fun `convert from turns to degrees`() {
        val angle = Angle.fromTurns(1.5)

        assertEquals(540.0, angle.degrees, 0.001)
    }

    // Normalized
    @Test
    fun `given the number of rotations is greater than one, then normalize the angle`() {
        val angle = Angle.fromTurns(1.5)

        assertEquals(0.5, angle.normalized.turns, 0.001)
    }

}