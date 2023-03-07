package com.wave.talend.components.processor;

import java.io.Serializable;

import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.widget.Credential;
import org.talend.sdk.component.api.meta.Documentation;

@GridLayout({
    // the generated layout put one configuration entry per line,
    // customize it as much as needed
    @GridLayout.Row({ "secretID" }),
    @GridLayout.Row({ "secretKey" }),
    @GridLayout.Row({ "regionName" }),
    @GridLayout.Row({ "bucketName" }),
    @GridLayout.Row({ "keyName" }),
    @GridLayout.Row({ "owFlag" }),
    @GridLayout.Row({ "wordSeparator" }),
    @GridLayout.Row({ "batchCnt" })
})
@Documentation("TODO fill the documentation for this configuration")
public class COSOutputProcessorConfiguration implements Serializable {
    @Credential
    @Option
    @Documentation("The secretID of tencent cos services")
    private String secretID;

    @Credential
    @Option
    @Documentation("The secretKey of tencent cos services")
    private String secretKey;

    @Option
    @Documentation("The region of tencent cos")
    private String regionName;

    @Option
    @Documentation("The bucket of tencent cos")
    private String bucketName;

    @Option
    @Documentation("The key of tencent cos")
    private String keyName;

    @Option
    @Documentation("Overwrite Flag: overwrite object or not")
    private String owFlag;

    @Option
    @Documentation("The separator for words")
    private String wordSeparator;

    @Option
    @Documentation("The amount for one batch")
    private int batchCnt;

    public String getSecretID() {
        return secretID;
    }

    public COSOutputProcessorConfiguration setSecretID(String secretID) {
        this.secretID = secretID;
        return this;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public COSOutputProcessorConfiguration setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public String getRegionName() {
        return regionName;
    }

    public COSOutputProcessorConfiguration setRegionName(String regionName) {
        this.regionName = regionName;
        return this;
    }

    public String getBucketName() {
        return bucketName;
    }

    public COSOutputProcessorConfiguration setBucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public String getKeyName() {
        return keyName;
    }

    public COSOutputProcessorConfiguration setKeyName(String keyName) {
        this.keyName = keyName;
        return this;
    }

    public String getOwFlag() {
        return owFlag;
    }

    public COSOutputProcessorConfiguration setOwFlag(String owFlag) {
        this.owFlag = owFlag;
        return this;
    }

    public String getWordSeparator() {
        return wordSeparator;
    }

    public COSOutputProcessorConfiguration setWordSeparator(String wordSeparator) {
        this.wordSeparator = wordSeparator;
        return this;
    }

    public int getBatchCnt() {
        return batchCnt;
    }

    public COSOutputProcessorConfiguration setBatchCnt(int batchCnt) {
        this.batchCnt = batchCnt;
        return this;
    }
}