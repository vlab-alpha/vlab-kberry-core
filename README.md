# V.Lab Kberry Core (BAOS / FT1.2)

Kberry Core is a Java-based library for reliable, low-level communication with **KNX BAOS (Binary Application Object Server)** devices over **FT1.2 / serial** connections.  
It focuses on **correct protocol handling**, **asynchronous behavior**, and **clean separation between Requests, Responses, and Indications**.

The library is designed for developers who want **full control** over KNX communication without ETS or opaque SDKs.

---
![5637156979_40 jpg](https://github.com/user-attachments/assets/abc63619-b744-4881-8a02-ecbd0469dfb1)



## Motivation

One of the main reasons for developing this library was the lack of a suitable solution in existing tools.
Calimero did not provide a reliable or fast mechanism to receive immediate updates when values change, and Weinzierl officially offers libraries only for C# and C++.
KNX BAOS devices behave differently from typical request/response protocols:

- Responses can be **delayed**
- Values may arrive **as Indications**
- Cache updates are **asynchronous**
- KNX Data subscription.
- Multiple frames may reference the **same datapoint ID**
- Serial timing and blocking behavior matter

Kberry Core was built to handle these realities **correctly and deterministically**.

---

## Features

- FT1.2 frame parsing and generation
- Full BAOS Object Server support
- Proper handling of:
  - Requests
  - Responses
  - Indications
- Asynchronous datapoint updates
- Explicit ACK handling
- Thread-safe reader/writer architecture
- No hidden retries or magic behavior
- Designed for **production-grade KNX integrations**

---

## Core Components

### `SerialBAOSConnection`
High-level API for:
- Reading datapoints
- Writing datapoints
- Listening to value changes
- Handling server item states

This is the main entry point for applications.

---

### `BAOSWriter`
Responsible for:
- Sending FT1.2 frames
- Managing ODD / EVEN toggle
- Sending ACKs
- Reset handling

No implicit retries or delays.

---

### `BAOSReader`
Responsible for:
- Reading raw serial bytes
- Parsing FT1.2 frames
- Separating:
  - Responses
  - Indications
  - ACKs
- Dispatching frames to listeners or futures

---

### FT1.2 Parser
- Stream-based
- Handles partial frames
- Safe against serial timing issues

---

## Usage Example

### Connect

```java
// Serial Interface; Timeout; Retry
SerialBAOSConnection connection = new SerialBAOSConnection("/dev/ttyAMA0", 1000, 10);
KNXDevices devices = new KNXDevices(connection);
// House is a enum who implemented the interface PositionPath 
devices.register(PresenceSensor.at(House.OfficeTop)); 
devices.register(Light.at(House.KitchenWall));
// ... register all devices with the specified Position

// The export is necessary for the CSV-based import process of the Weinzierl BAOS Importer.
// [https://weinzierl.de/images/download/products/dca/baoscsvimporter/weinzierl_dca_baos_csv_importer_manual_de.pdf]
devices.exportCSV(Path.of("weinzierl_export.csv"));

connection.connect();
connection.disconnect();

```

### Get Devices
All registered devices can be accessed through the Devices section, where they can also be searched. 
```java
// Get all Humidity Sensors
var myListOfDevices = devices.getKNXDevices(HumiditySensor.class);
// Get device from position Path
var myOptionalDevice = devices.getKNXDevice(PresenceSensor.class, House.OfficeTop);
// Get all push in the room
var myPushDeviceFromKitchen = devices.getKNXDevicesByRoom(Push.class, "kitchen");
```

### Device
Each device provides individual functions. For example, a light can be switched on and off, while a floor heating system allows setting the operating mode and target temperature.
In addition, each device has its own observer mechanism, allowing interested parties to subscribe and receive updates whenever a value changes, such as temperature or presence.
```java
devices.getKNXDevice(PresenceSensor.class, House.Office).get().addListener((sensor, available) -> {
    var isAvailable = available;
    var timeInSecond = sensor.getLastPresentSecond();
    System.out.println("Somone "+(!isAvailable?"was":"")+" in the room for"+(timeInSecond)+" second ago!");
});
devices.getKNXDevice(Light.class, HausTester.Office).get().on();
```

## Device
### Fast Updates & Indicators
When a value changes (for example, a presence sensor), the update is received and processed within approximately 300 ms.
Indicator messages always have priority, including over regular Object Server responses.
This ensures that state changes are propagated immediately and are not delayed by polling or request/response cycles.

### Local Caching & Persistence
All values are buffered in the application’s memory.
When a value is requested, it is always served directly from the application RAM, ensuring fast and deterministic access.

In addition, all values are persisted to disk.




