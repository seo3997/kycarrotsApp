package com.whomade.kycarrots.ui.common;

import java.io.Serializable;

/**
 * 사는 곳, 광고 기본발송 Data Info
 * 공통코드 결과 값
 */
public class TxtListDataInfo implements Serializable {
    private String strIdx=""; //선택 한 고유 값
    private String strMsg="";

    public String getStrIdx() {
        return strIdx;
    }

    public void setStrIdx(String strIdx) {
        this.strIdx = strIdx;
    }

    public String getStrMsg() {
        return strMsg;
    }

    public void setStrMsg(String strMsg) {
        this.strMsg = strMsg;
    }
}
