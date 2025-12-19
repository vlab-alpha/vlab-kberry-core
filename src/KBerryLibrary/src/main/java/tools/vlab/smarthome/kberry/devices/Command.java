package tools.vlab.smarthome.kberry.devices;

import tools.vlab.smarthome.kberry.baos.KnxDatapointType;

// FIXME: Aufräumem vieles wird nicht benutzt
public enum Command {
    // --- Allgemeine Schalter / Zustand (DPT 1: BOOLEAN) ---
    ON_OFF_SWITCH(Communication.READWRITE, KnxDatapointType.BOOLEAN),
    ALARM_STATUS(Communication.READ, KnxDatapointType.BOOLEAN),
    PRESENCE_STATUS(Communication.READ, KnxDatapointType.BOOLEAN),
    GENERIC_STATUS_FLAG(Communication.READ, KnxDatapointType.BOOLEAN),
    ON_OFF_STATUS(Communication.READ, KnxDatapointType.BOOLEAN),
    STOP(Communication.WRITE, KnxDatapointType.BOOLEAN),
    SHUTTER_REFERENCE(Communication.WRITE, KnxDatapointType.BOOLEAN),
    WINDOW_CONTACT_STATUS(Communication.READ, KnxDatapointType.BOOLEAN),
    COMFORT_MODE_ACTIVE_STATUS(Communication.READ, KnxDatapointType.BOOLEAN),
    FROST_PROTECTION_ACTIVE_STATUS(Communication.READ, KnxDatapointType.BOOLEAN),
    SET_BRIGHTNESS(Communication.WRITE, KnxDatapointType.INT8),
    BRIGHTNESS_STATUS(Communication.READ, KnxDatapointType.INT8),
    ENABLE(Communication.WRITE, KnxDatapointType.BOOLEAN),
    ENABLE_STATUS(Communication.READ, KnxDatapointType.BOOLEAN),

    // --- Jalousie / Beschattung (DPT 5: UINT8) ---
    // Der Typ SINT8 aus dem vorherigen Code war DPT 6.xxx.
    // Wir nutzen hier den spezifischeren DPT 5.001 (Prozent).
    SHUTTER_UP_DOWN_CONTROL(Communication.WRITE, KnxDatapointType.UINT8), // DPT 5 (Position 0-100%)
    SHUTTER_POSITION_SET(Communication.WRITE, KnxDatapointType.UINT8),   // Positionsvorgabe 0-100%
    SHUTTER_POSITION_ACTUAL_STATUS(Communication.READ, KnxDatapointType.UINT8), // Ist-Position 0-100%

    // --- Sensoren / Messwerte (DPT 9: FLOAT9) ---
    TEMPERATURE_ACTUAL(Communication.READ, KnxDatapointType.FLOAT9), // Nutzt den generischen DPT 9 Float Typ
    HUMIDITY_ACTUAL(Communication.READ, KnxDatapointType.FLOAT9),   // Nutzt den generischen DPT 9 Float Typ
    CO2_ACTUAL(Communication.READ, KnxDatapointType.FLOAT9),       // Nutzt den generischen DPT 9 Float Typ
    ELECTRICITY_KWH_ACTUAL(Communication.READ, KnxDatapointType.FLOAT9), // Nutzt den generischen DPT 9 Float Typ
    ELECTRICITY_KWH_METER(Communication.READ, KnxDatapointType.SINT32), // Zählerstand kWh (DPT 13.010 oder ähnliches)
    ELECTRICITY_W_ACTUAL(Communication.READ, KnxDatapointType.FLOAT32), // Aktuelle Leistung in Watt (W)
    ELECTRICITY_A_ACTUAL(Communication.READ, KnxDatapointType.FLOAT32), // Aktuelle Stromstärke in Ampere (A)
    ELECTRICITY_V_ACTUAL(Communication.READ, KnxDatapointType.FLOAT32), // Aktuelle Spannung in Volt (V)
    ELECTRICITY_POWER_FACTOR(Communication.READ, KnxDatapointType.FLOAT32), // Leistungsfaktor Cos(Phi)
    LUX_VALUE_ACTUAL(Communication.READ, KnxDatapointType.FLOAT9),     // Nutzt den generischen DPT 9 Float Typ
    VOC_ACTUAL(Communication.READ, KnxDatapointType.FLOAT9), // Nutzt DPT 9.xxx (ppm oder µg/m³)


    // --- Heizung/Klima/MDT AKH spezifisch (DPT 9: FLOAT9, DPT 20: HVAC_MODE) ---
    HVAC_SETPOINT_TEMPERATURE_SET(Communication.WRITE, KnxDatapointType.FLOAT9),
    HVAC_SETPOINT_TEMPERATURE_ACTUAL(Communication.READ, KnxDatapointType.FLOAT9),
    HVAC_OPERATING_MODE_SET(Communication.WRITE, KnxDatapointType.HVAC_MODE),
    HVAC_OPERATING_MODE_ACTUAL(Communication.READ, KnxDatapointType.HVAC_MODE),
    HVAC_ACTUATOR_POSITION_ACTUAL(Communication.READ, KnxDatapointType.FLOAT9),

    // RGB
    RGB_COLOR_CONTROL(Communication.WRITE, KnxDatapointType.RGB),
    RGB_COLOR_STATUS(Communication.READ, KnxDatapointType.RGB),

    // --- Datum / Zeit / System (DPT 10, DPT 11, DPT 17) ---
    SYSTEM_TIME_ACTUAL(Communication.READ, KnxDatapointType.TIME),
    SYSTEM_DATE_ACTUAL(Communication.READ, KnxDatapointType.DATE),
    SCENE_ACTIVATION(Communication.WRITE, KnxDatapointType.SCENE_NUMBER),
    ;


    public final Communication communication;
    public final KnxDatapointType dataType;

    Command(Communication communication, KnxDatapointType dataType) {
        this.communication = communication;
        this.dataType = dataType;
    }

    public String getId() {
        return this.name().trim().replaceAll(" ", "_").toUpperCase();
    }
}
