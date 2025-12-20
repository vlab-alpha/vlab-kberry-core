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