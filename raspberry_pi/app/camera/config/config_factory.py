# pylint: disable=C0114,C0115,C0116,E0401,R0903,R0911
from datetime import datetime

from app.camera.config.config_ardu_mjpeg import ConfigArduMjpeg
from app.camera.config.config_gs_color import ConfigGsColor
from app.camera.config.config_gs_mono import ConfigGsMono
from app.camera.config.config_protocol import Config
from app.camera.config.config_thrifty_mjpeg import ConfigThriftyMjpeg
from app.camera.size import Size
from app.camera.model import Model
from app.config.identity import Identity



class ConfigFactory:
    """Choose a camera confing based on identity."""

    @staticmethod
    def get(identity: Identity, model: Model) -> Config:
        print(f"[{datetime.now()}] " + "*** CameraFactory for identity:", identity)
        match identity:
            case Identity.FUNNEL:
                # test the RPi GS camera
                # return ConfigGsMono(Size.from_model(model))
                # test the ThriftyCam
                return ConfigThriftyMjpeg(Size.from_model(model))
                # return ConfigThriftyYuyv(Size.from_model(model))
            case Identity.CLIMB_RIGHT:
                return ConfigThriftyMjpeg(Size.from_model(model))
            case Identity.CLIMB_LEFT:
                return ConfigGsColor(Size.from_model(model))
            case Identity.SHOOTER:
                return ConfigGsMono(Size.from_model(model))
            case Identity.CAMERA_FRONT:
                return ConfigArduMjpeg(model)
            case Identity.CAMERA_2A:
                return ConfigArduMjpeg(model)
            case Identity.COLOR_DETECT_1:
                return ConfigArduMjpeg(model)
            case Identity.BALL_DETECT:
                return ConfigArduMjpeg(model)
            case _:
                return ConfigGsMono(Size.from_model(model))
