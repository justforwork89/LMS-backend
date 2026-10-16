package com.dy.lms.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommonCode {
    private String groupCd;
    private String code;
    private String codeName;
    private int sortOrder;
    private String useYn;
}