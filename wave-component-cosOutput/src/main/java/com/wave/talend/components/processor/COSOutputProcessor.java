package com.wave.talend.components.processor;

import static org.talend.sdk.component.api.component.Icon.IconType.CUSTOM;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.AppendObjectRequest;
import com.qcloud.cos.model.AppendObjectResult;
import com.qcloud.cos.model.ObjectMetadata;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.component.Version;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.meta.Documentation;
import org.talend.sdk.component.api.processor.AfterGroup;
import org.talend.sdk.component.api.processor.BeforeGroup;
import org.talend.sdk.component.api.processor.ElementListener;
import org.talend.sdk.component.api.processor.Input;
import org.talend.sdk.component.api.processor.Output;
import org.talend.sdk.component.api.processor.OutputEmitter;
import org.talend.sdk.component.api.processor.Processor;
import org.talend.sdk.component.api.record.Record;

import com.wave.talend.components.service.WaveComponentService;
import org.talend.sdk.component.api.record.Schema;

@Version(1) // default version is 1, if some configuration changes happen between 2 versions you can add a migrationHandler
@Icon(value = CUSTOM, custom = "COSOutput") // icon is located at src/main/resources/icons/COSOutput.svg
@Processor(name = "COSOutput")
@Documentation("TODO fill the documentation for this processor")
public class COSOutputProcessor implements Serializable {
    private final COSOutputProcessorConfiguration configuration;
    private final WaveComponentService service;

    private COSClient cosClient;
    private long position;
    private String bucketName;
    private String keyName;
    private String separator;

    private StringBuilder buffer;
    private int batchCnt;
    private int currentCnt;

    public COSOutputProcessor(@Option("configuration") final COSOutputProcessorConfiguration configuration,
                          final WaveComponentService service) {
        this.configuration = configuration;
        this.service = service;
    }

    @PostConstruct
    public void init() {
        // this method will be executed once for the whole component execution,
        // this is where you can establish a connection for instance
        // Note: if you don't need it you can delete it

        bucketName = this.configuration.getBucketName();
        keyName = this.configuration.getKeyName();
        separator = this.configuration.getWordSeparator();
        batchCnt = this.configuration.getBatchCnt();
        String secretID = this.configuration.getSecretID();
        String secretKey = this.configuration.getSecretKey();
        String regionName = this.configuration.getRegionName();

        cosClient = COSUtils.createCOSClient(secretID, secretKey, regionName);

        if (this.configuration.getOwFlag().equalsIgnoreCase("Y")) {
            boolean exist = cosClient.doesObjectExist(bucketName, keyName);
            if (exist)
                cosClient.deleteObject(bucketName, keyName);
            position = 0L;
        } else {
            ObjectMetadata objectMetadata = cosClient.getObjectMetadata(bucketName, keyName);
            position = objectMetadata.getContentLength();
        }

        buffer = new StringBuilder();
        currentCnt = 0;
    }

    @BeforeGroup
    public void beforeGroup() {
        // if the environment supports chunking this method is called at the beginning if a chunk
        // it can be used to start a local transaction specific to the backend you use
        // Note: if you don't need it you can delete it
    }

    @ElementListener
    public void onNext(
            @Input final Record defaultInput,
            @Output final OutputEmitter<Record> defaultOutput) {
        // this is the method allowing you to handle the input(s) and emit the output(s)
        // after some custom logic you put here, to send a value to next element you can use an
        // output parameter and call emit(value).
        Schema schema = defaultInput.getSchema();
        List<Schema.Entry> entries = schema.getEntries();
        Object colValue;
        for (int i = 0; i < entries.size(); i++) {
            Schema.Entry entry = entries.get(i);
            String colName = entry.getName();
            Schema.Type type = entry.getType();
            switch (type) {
                case INT:
                    colValue = defaultInput.getInt(colName);
                    break;
                case LONG:
                    colValue = defaultInput.getLong(colName);
                    break;
                case FLOAT:
                    colValue = defaultInput.getFloat(colName);
                    break;
                case DOUBLE:
                    colValue = defaultInput.getDouble(colName);
                    break;
                case DECIMAL:
                    colValue = defaultInput.getDecimal(colName);
                    break;
                case BOOLEAN:
                    colValue = defaultInput.getBoolean(colName);
                    break;
                case DATETIME:
                    colValue = defaultInput.getDateTime(colName);
                    break;
                default:
                    colValue = defaultInput.getString(colName);
            }

            // System.out.println(colName + " : " + colValue);
            if (null != colValue)
                buffer.append(colValue);
            if (i != entries.size() - 1)
                buffer.append(this.separator);
        }
        buffer.append("\n");
        currentCnt++;

        if (currentCnt == batchCnt) {
            // System.out.println(buffer);
            byte data[] = buffer.toString().getBytes();
            InputStream inputStream = new ByteArrayInputStream(data);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(data.length);

            try {
                AppendObjectRequest appendObjectRequest = new AppendObjectRequest(bucketName, keyName, inputStream, objectMetadata);
                appendObjectRequest.setPosition(position);
                AppendObjectResult appendObjectResult = cosClient.appendObject(appendObjectRequest);
                position = appendObjectResult.getNextAppendPosition();
                // System.out.println(position);
            } catch (CosClientException e) {
                e.printStackTrace();
            }
            buffer = new StringBuilder();
            currentCnt = 0;
        }
    }

    @AfterGroup
    public void afterGroup() {
        // symmetric method of the beforeGroup() executed after the chunk processing
        // Note: if you don't need it you can delete it
    }

    @PreDestroy
    public void release() {
        // this is the symmetric method of the init() one,
        // release potential connections you created or data you cached
        // Note: if you don't need it you can delete it
        if (currentCnt != 0) {
            // System.out.println(buffer);
            byte data[] = buffer.toString().getBytes();
            InputStream inputStream = new ByteArrayInputStream(data);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(data.length);

            try {
                AppendObjectRequest appendObjectRequest = new AppendObjectRequest(bucketName, keyName, inputStream, objectMetadata);
                appendObjectRequest.setPosition(position);
                AppendObjectResult appendObjectResult = cosClient.appendObject(appendObjectRequest);
                position = appendObjectResult.getNextAppendPosition();
                // System.out.println(position);
            } catch (CosClientException e) {
                e.printStackTrace();
            }
            buffer = new StringBuilder();
            currentCnt = 0;
        }

        cosClient.shutdown();
        position = 0L;
    }
}