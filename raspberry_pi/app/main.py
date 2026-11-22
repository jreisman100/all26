# pylint: disable=C0116,C0413,E0401,R0914
"""This is the coprocessor main loop.

Each task is run by its own Looper, in its own thread.

You can't run this from the command line.  To run the app,
use the script called "runapp.py" in the raspberry_pi directory
(one level above this one).
"""

import time
from datetime import datetime
from threading import Event, Thread

from app.camera.camera_factory import CameraFactory
from app.camera.camera_loop import CameraLoop
from app.camera.camera_protocol import Camera
from app.config.identity import Identity
from app.dashboard.display_factory import DisplayFactory
from app.dashboard.display_protocol import Display
from app.interpreter.interpreter_factory import InterpreterFactory
from app.interpreter.interpreter_protocol import Interpreter
from app.network.network_protocol import Network
from app.network.real_network import RealNetwork
from app.util.banner import print_banner
from app.util.timestamps import Timestamps


def main() -> None:
    print_banner()
    print(f"[{datetime.now()}] " + "*** Starting main.py")
    identity: Identity = Identity.get()
    done: Event = Event()  # to shut down all threads
    thread: Thread | None = None
    try:

    
        cameras: list[Camera] = CameraFactory.get(identity)

        threads = []

        network: Network = RealNetwork(identity, done)
        timestamps = Timestamps(network)
        
        for device in cameras:
        # main display for annotated images

            display1: Display = DisplayFactory.get(identity, device, f"display{device.num}-1", 0.25)
            # secondary display
            display2: Display = DisplayFactory.get(identity, device, f"display{device.num}-2", 0.25)
            
            interpreter: Interpreter = InterpreterFactory.get(
                identity, device, display1, display2, network, timestamps
            )

            camera_loop: CameraLoop = CameraLoop(device, interpreter, done)
            threads.append(
                Thread(target=camera_loop.run)
            )

        print(f"[{datetime.now()}] " + "*** Starting the looper thread")
        for thread in threads: thread.start()
        # looper.run will return when done, so wait for that.
        for thread in threads: thread.join()
        print(f"[{datetime.now()}] " + "*** Looper has exited")

    finally:
        done.set()  # exit all threads cleanly
        # if the loop is hung, we don't want to wait for it,
        # so just wait a little bit of time instead.
        time.sleep(1)
