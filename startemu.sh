#!/bin/bash
prime-run ~/Android/Sdk/emulator/emulator -avd $(~/Android/Sdk/emulator/emulator -list-avds | sed -n $1p) -netdelay none -netspeed full
