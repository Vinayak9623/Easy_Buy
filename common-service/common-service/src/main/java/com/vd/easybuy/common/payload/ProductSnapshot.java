package com.vd.easybuy.common.payload;

import java.util.UUID;


public record ProductSnapshot(

        UUID id,
        String title,
        String shortDesc,
        String longDesc,
        Double price,
        Integer discount,
        Boolean live

) {
}
