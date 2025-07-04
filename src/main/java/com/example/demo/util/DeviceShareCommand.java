package com.example.demo.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class DeviceShareCommand {
    private String type;
    private UUID correlationId;
    @JsonProperty("stream_target_name")
    private String deviceName;
    @JsonProperty("stream_duration_sec")
    private Integer duration;
    @JsonProperty("stream_interval_sec")
    private Integer interval;
 }
