# V.Lab Kberry Core (BAOS / FT1.2)

Kberry Core is a Java-based library for reliable, low-level communication with **KNX BAOS (Binary Application Object Server)** devices over **FT1.2 / serial** connections.  
It focuses on **correct protocol handling**, **asynchronous behavior**, and **clean separation between Requests, Responses, and Indications**.

The library is designed for developers who want **full control** over KNX communication without ETS or opaque SDKs.

---

## Motivation

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

## Architecture Overview
┌──────────────┐
│ Application  │
└──────┬───────┘
│
┌──────▼────────┐
│ SerialBAOS    │  High-level API
│ Connection    │
└──────┬────────┘
│
┌──────▼────────┐      ┌──────────────┐
│ BAOSWriter    │ —> │ Serial Port  │
│ (FT1.2)       │      └──────────────┘
└───────────────┘
┌───────────────┐
│ BAOSReader    │ <— Incoming Frames
│ + Parser      │
└───────────────┘

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
SerialBAOSConnection conn =
    new SerialBAOSConnection("/dev/ttyUSB0", 1000, 3);

conn.connect();

### Response
DataPoint value = conn.read(new DataPointId(10));

### Request
conn.write(DataPoint.bool(new DataPointId(4), true));

### Indicator
conn.onValueChanged(new DataPointId(10), dp -> {
    System.out.println("New value: " + dp);
});




