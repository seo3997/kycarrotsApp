package com.whomade.kycarrots.ui.ad.makead;

import java.io.Serializable;

/**
 * 광고 상세보기 정보
 */
public class ADDetailInfo implements Serializable {
    private String strTitleImgUrl=""; //광고 title 이미지 url
    private String strGrade=""; //평점
    private String strADName=""; //광고명
    private String strDetailTxt=""; //광고간단 설명
    private String strADTel=""; //광고주 전화
    private String strHomepageUrl=""; //홈페이지 Url
    private String strDetailImgUrl1=""; //광고 세부이미지1
    private String strDetailImgUrl2=""; //광고 세부이미지2
    private String strDetailImgUrl3=""; //광고 세부이미지3
//    private String strADDuration=""; //광고 기간
    private String strDateS=""; //광고 시작
    private String strDateE=""; //광고 마감
    private String strADAddress=""; //광고 주소
    private String strAdvertiserImgUrl=""; //광고주 사진
    private String strTradeName=""; //상호명
    private String strRepresentativeName=""; //대표자
    private String strStrRepresentativeTel; //대표전화
    private String strAdvertiserAddres; //광고주 주소
    private String strADLike; //관심광고 여부 (0: 관심광고 아님, 1; 관심광고)

    private String strADAmount; //광고 남은 금액
    private String strADMinAmount; //광고 최소 금액

    public String getStrTitleImgUrl() {
        return strTitleImgUrl;
    }

    public void setStrTitleImgUrl(String strTitleImgUrl) {
        this.strTitleImgUrl = strTitleImgUrl;
    }

    public String getStrGrade() {
        return strGrade;
    }

    public void setStrGrade(String strGrade) {
        this.strGrade = strGrade;
    }

    public String getStrADName() {
        return strADName;
    }

    public void setStrADName(String strADName) {
        this.strADName = strADName;
    }

    public String getStrDetailTxt() {
        return strDetailTxt;
    }

    public void setStrDetailTxt(String strDetailTxt) {
        this.strDetailTxt = strDetailTxt;
    }

    public String getStrADTel() {
        return strADTel;
    }

    public void setStrADTel(String strADTel) {
        this.strADTel = strADTel;
    }

    public String getStrHomepageUrl() {
        return strHomepageUrl;
    }

    public void setStrHomepageUrl(String strHomepageUrl) {
        this.strHomepageUrl = strHomepageUrl;
    }

    public String getStrDetailImgUrl1() {
        return strDetailImgUrl1;
    }

    public void setStrDetailImgUrl1(String strDetailImgUrl1) {
        this.strDetailImgUrl1 = strDetailImgUrl1;
    }

    public String getStrDetailImgUrl2() {
        return strDetailImgUrl2;
    }

    public void setStrDetailImgUrl2(String strDetailImgUrl2) {
        this.strDetailImgUrl2 = strDetailImgUrl2;
    }

    public String getStrDetailImgUrl3() {
        return strDetailImgUrl3;
    }

    public void setStrDetailImgUrl3(String strDetailImgUrl3) {
        this.strDetailImgUrl3 = strDetailImgUrl3;
    }

    public String getStrDateS() {
        return strDateS;
    }

    public void setStrDateS(String strDateS) {
        this.strDateS = strDateS;
    }

    public String getStrDateE() {
        return strDateE;
    }

    public void setStrDateE(String strDateE) {
        this.strDateE = strDateE;
    }

    public String getStrADAddress() {
        return strADAddress;
    }

    public void setStrADAddress(String strADAddress) {
        this.strADAddress = strADAddress;
    }

    public String getStrAdvertiserImgUrl() {
        return strAdvertiserImgUrl;
    }

    public void setStrAdvertiserImgUrl(String strAdvertiserImgUrl) {
        this.strAdvertiserImgUrl = strAdvertiserImgUrl;
    }

    public String getStrTradeName() {
        return strTradeName;
    }

    public void setStrTradeName(String strTradeName) {
        this.strTradeName = strTradeName;
    }

    public String getStrRepresentativeName() {
        return strRepresentativeName;
    }

    public void setStrRepresentativeName(String strRepresentativeName) {
        this.strRepresentativeName = strRepresentativeName;
    }

    public String getStrStrRepresentativeTel() {
        return strStrRepresentativeTel;
    }

    public void setStrStrRepresentativeTel(String strStrRepresentativeTel) {
        this.strStrRepresentativeTel = strStrRepresentativeTel;
    }

    public String getStrAdvertiserAddres() {
        return strAdvertiserAddres;
    }

    public void setStrAdvertiserAddres(String strAdvertiserAddres) {
        this.strAdvertiserAddres = strAdvertiserAddres;
    }

    public String getStrADLike() {
        return strADLike;
    }

    public void setStrADLike(String strADLike) {
        this.strADLike = strADLike;
    }

    public String getStrADAmount() {
        return strADAmount;
    }

    public void setStrADAmount(String strADAmount) {
        this.strADAmount = strADAmount;
    }

    public String getStrADMinAmount() {
        return strADMinAmount;
    }

    public void setStrADMinAmount(String strADMinAmount) {
        this.strADMinAmount = strADMinAmount;
    }
}
