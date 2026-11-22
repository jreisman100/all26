# pylint: disable=C0114,C0115,C0116,E0401,R0801
from app.camera.size import Size
from app.camera.model import Model
from app.camera.config.config_ardu_BO61O_mjpeg import ConfigArduMjpegBO61O
from app.camera.config.config_ardu_OV9782_mjpeg import ConfigArduMjpegOV9782


def ConfigArduMjpeg(model: Model):

    match model:

        case Model.ARDU_BO61O:

            return ConfigArduMjpegBO61O(Size.from_model(model))

        case Model.ARDU_OV9782:

            return ConfigArduMjpegOV9782(Size.from_model(model))

        case _:

            return ConfigArduMjpegBO61O(Size.from_model(model))
