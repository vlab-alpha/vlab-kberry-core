# vlab-kberry-core

`vlab-kberry-core` is a lightweight Java framework for implementing Smart Home logic without the overhead of large automation platforms, complex UI systems, or heavy configuration.

The main idea behind the project is simple:

> Access Smart Home devices through a unified Java API and implement automation logic directly in code.

The framework provides abstractions for different Smart Home technologies such as:

* KNX devices
* Custom MQTT devices
* Shelly devices

Instead of building automations through graphical editors or complicated rule engines, developers can implement their Smart Home logic directly in Java using simple enums and device abstractions.

---

# Goal of the Project

Most Smart Home systems require:

* large configuration files
* UI-based automation editors
* vendor-specific integrations
* additional middleware
* complicated deployment structures

`vlab-kberry-core` focuses on the opposite approach:

* Simple Java-first development
* Direct device access
* Unified device handling
* Minimal setup overhead
* Clean and maintainable logic implementations

The goal is to allow developers to write Smart Home automation logic like regular backend software.

---

# Core Idea

Devices are organized through enums and reusable abstractions.

Instead of manually searching for MQTT topics, KNX group addresses, or Shelly endpoints everywhere in the code, devices are centrally defined and can be reused throughout the application.

This enables:

* strongly typed device access
* centralized device management
* reusable automation logic
* cleaner code structure
* easier maintenance

---

# Supported Technologies

Currently supported integrations include:

## KNX

Access KNX devices such as:
* Jalousies
* Lights
* LUX sensors
* Floor heater
* LED (RGB)
* Dimmer
* Temperature sensor
* VOC sensor
* Presence sensor
* Humidity sensor
* Push button

## MQTT Custom Devices

Custom MQTT devices can be integrated using a simple abstraction layer.
Currently implemented example:
* FAN device

## Shelly Devices

Simple access to Shelly devices over the network.
Example:
* Shelly Plug
* Shelly LED
---

# Example Usage

## 1. Custom MQTT FAN

Example of controlling a custom MQTT fan device.

```java
CustomMqttDevices customMqttDevices = new CustomMqttDevices("mqtt broker url");
customMqttDevices.getDevice(Fan.class, Haus.BathWall).ifPresent(fan -> fan.setSpeed(2));
```

Example idea:

* Automatically enable the fan when humidity rises.
* Turn off the fan after a timeout.

---

## 2. Shelly Plug

Example of accessing a Shelly Plug device.

```java
ShellyDevices shellyDevice = new ShellyDevices("mqtt broker url");
shellyDevice.getDevicesByRoom(Plug.class, "myRoom").forEach(Plug::on);
```

Example idea:

* Automatically switch off standby devices.
* Enable devices based on schedules or presence.

---

## 3. KNX Light

Example of controlling KNX blinds.

```java
devices.register(Light.at(Haus.BathTop));
devices.exportCSV(Path.of("weinzierl_export.csv"));
devices.getKNXDevices(Light.class).forEach(Light::on);
```

Example idea:

* register Device at first.
* Export the csv file (important for the ETS Weinzierl import).
* Search device and add action.

---

# Philosophy

`vlab-kberry-core` is intentionally focused on code-driven automation.

It is designed for developers who:

* prefer writing logic in Java
* want full control over their automations
* dislike heavy UI-based systems
* want reusable and testable Smart Home logic
* prefer software engineering principles over click-based automation editors

---

# Architecture Overview

The framework separates:

* device access
* communication layers
* automation logic

This allows automation logic to remain clean and independent from the underlying transport technology.

Example:

```text
Automation Logic
        ↓
Device Abstractions
        ↓
KNX / MQTT / Shelly
```

---

# Example Automation Logic

```java
if (bathroomHumidity.isAbove(70)) {
    bathroomFan.turnOn();
}

if (presenceDetector.isAbsentForMinutes(10)) {
    livingroomLights.turnOff();
}
```

The goal is to keep Smart Home automation readable and close to natural business logic.

---

# Use Cases

Typical use cases:

* Smart Home automation
* Building automation
* Presence-based logic
* Climate control
* Energy optimization
* Custom device integrations
* MQTT-based IoT systems
* KNX automation projects

---

# Design Principles

* Java-first
* Minimal configuration
* Reusable abstractions
* Technology-independent logic
* Clean code structure
* Developer-oriented
* Lightweight architecture

---

# Status

The project is under active development.

Current focus:

* improving device abstractions
* extending KNX support
* extending MQTT custom devices
* additional Shelly integrations
* simplifying Smart Home logic implementation

---

# Repository

GitHub Repository:

[https://github.com/vlab-alpha/vlab-kberry-core](https://github.com/vlab-alpha/vlab-kberry-core)
