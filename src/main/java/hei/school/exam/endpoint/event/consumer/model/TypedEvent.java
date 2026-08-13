package hei.school.exam.endpoint.event.consumer.model;

import hei.school.exam.PojaGenerated;
import hei.school.exam.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
