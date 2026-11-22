package controls

import keyBinding.Key
import keyBinding.Keymap
import keyBinding.plus

val DefaultKeymap = Keymap(
    Key.CONTROL + Key.SHIFT + Key.UP to ServerCommand.GAIN_UP,
    Key.CONTROL + Key.SHIFT + Key.DOWN to ServerCommand.GAIN_DOWN,
    Key.CONTROL + Key.SHIFT + Key.RIGHT to ServerCommand.INCREASE_NOISE_FLOOR,
    Key.CONTROL + Key.SHIFT + Key.LEFT to ServerCommand.DECREASE_NOISE_FLOOR,
)