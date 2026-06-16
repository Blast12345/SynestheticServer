package dsp.clean

import extensions.minus
import extensions.times
import org.apache.commons.math3.complex.Complex
import kotlin.math.*

data class CleanResult<Component, Data>(
    val components: List<Component>,
    val residual: Data
)

interface CleanComponent {
    val magnitude: Double
}

// TODO: Reference
abstract class CleanAlgorithm<Data, Component : CleanComponent> {

    fun clean(
        dirtyData: Data,
        componentResponse: (Component) -> Data,
        loopGain: Double,
        maxIterations: Int,
        magnitudeThreshold: Double
    ): CleanResult<Component, Data> {
        require(maxIterations > 0) { "The number of iterations must be greater than 0" }
        require(magnitudeThreshold >= 0.0) { "The magnitude threshold must greater than or equal to 0" }
        require(loopGain > 0.0 && loopGain <= 1.0) { "The loop gain must be between 0.0 and 1.0" }

        val components = mutableListOf<Component>()
        var residual = dirtyData

        repeat(maxIterations) {
            val component = findBiggestComponent(residual)

            if (component.magnitude < magnitudeThreshold) {
                return CleanResult(components, residual)
            }

            val scaledComponent = scaleComponent(component, loopGain)
            components.add(scaledComponent)

            val componentResponse = componentResponse(scaledComponent)
            residual = subtract(residual, componentResponse)
        }

        return CleanResult(components, residual)
    }


    abstract fun findBiggestComponent(residual: Data): Component
    abstract fun scaleComponent(component: Component, loopGain: Double): Component
    abstract fun subtract(data: Data, impulseResponse: Data): Data

}

data class InterpolatedCleanComponent1D(
    val position: Double,
    val value: Complex,
) : CleanComponent {
    override val magnitude: Double
        get() = value.abs()
}

class InterpolatedCleanAlgorithm1D : CleanAlgorithm<List<Complex>, InterpolatedCleanComponent1D>() {

    override fun findBiggestComponent(residual: List<Complex>): InterpolatedCleanComponent1D {
        val index = residual.withIndex().maxBy { it.value.abs() }.index
        return interpolateComponent(residual, index)
    }

    private fun interpolateComponent(residual: List<Complex>, index: Int): InterpolatedCleanComponent1D {
        if (index == 0 || index == residual.lastIndex) {
            return InterpolatedCleanComponent1D(index.toDouble(), residual[index])
        }

        val previous = residual[index - 1].abs()
        val current = residual[index].abs()
        val next = residual[index + 1].abs()

        val denominator = previous - 2 * current + next
        if (denominator == 0.0) {
            return InterpolatedCleanComponent1D(index.toDouble(), residual[index])
        }

        val delta = 0.5 * (previous - next) / denominator

        val interpolatedMagnitude = current - 0.25 * (previous - next) * delta

        val currentPhase = atan2(y = residual[index].imaginary, x = residual[index].real)
        val neighborPhase = if (delta >= 0) {
            atan2(residual[index + 1].imaginary, residual[index + 1].real)
        } else {
            atan2(residual[index - 1].imaginary, residual[index - 1].real)
        }

        var phaseDelta = neighborPhase - currentPhase
        phaseDelta -= 2 * PI * round(phaseDelta / (2 * PI))
        val interpolatedPhase = currentPhase + abs(delta) * phaseDelta

        val value = Complex(
            interpolatedMagnitude * cos(interpolatedPhase),
            interpolatedMagnitude * sin(interpolatedPhase)
        )

        return InterpolatedCleanComponent1D(index + delta, value)
    }

    override fun scaleComponent(
        component: InterpolatedCleanComponent1D,
        loopGain: Double
    ): InterpolatedCleanComponent1D {
        return InterpolatedCleanComponent1D(
            position = component.position,
            value = component.value * loopGain
        )
    }

    override fun subtract(
        data: List<Complex>,
        impulseResponse: List<Complex>
    ): List<Complex> {
        return data.zip(impulseResponse) { a, b -> a - b }
    }

}