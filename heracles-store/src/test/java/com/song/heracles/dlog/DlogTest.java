package com.song.heracles.dlog;

import org.apache.distributedlog.DLSN;
import org.apache.distributedlog.DistributedLogConfiguration;
import org.apache.distributedlog.DistributedLogConstants;
import org.apache.distributedlog.LogRecord;
import org.apache.distributedlog.LogRecordWithDLSN;
import org.apache.distributedlog.api.AsyncLogWriter;
import org.apache.distributedlog.api.DistributedLogManager;
import org.apache.distributedlog.api.namespace.Namespace;
import org.apache.distributedlog.api.namespace.NamespaceBuilder;
import org.apache.distributedlog.common.concurrent.FutureUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * @author song
 */
public class DlogTest {

	private static final Logger logger = LoggerFactory.getLogger(DlogTest.class);

	@Test
	public void write() throws Exception {

		DistributedLogManager dlm = null;
		Namespace namespace = null;
		AsyncLogWriter asyncLogWriter = null;
		try {
			URI uri = URI.create("distributedlog://127.0.0.1:7000/messaging/my_namespace");
			DistributedLogConfiguration conf = new DistributedLogConfiguration();
			conf.setImmediateFlushEnabled(true);
			conf.setOutputBufferSize(0);
			conf.setPeriodicFlushFrequencyMilliSeconds(0);
			conf.setLockTimeout(DistributedLogConstants.LOCK_IMMEDIATE);

			namespace = NamespaceBuilder.newBuilder()
				.conf(conf)
				.uri(uri)
				.regionId(DistributedLogConstants.LOCAL_REGION_ID)
				.clientId("console-writer")
				.build();

			/*
				OwnershipAcquireFailedException
			 */
			dlm = namespace.openLog("basic-stream-1");
			asyncLogWriter = FutureUtils.result(dlm.openAsyncLogWriter());
			LogRecord logRecord = new LogRecord(System.currentTimeMillis(), "Hello World".getBytes());
			DLSN dlsn = asyncLogWriter.write(logRecord).get();
			System.out.println(dlsn);
		} finally {
			if (null != asyncLogWriter) {
				FutureUtils.result(asyncLogWriter.asyncClose(), 5, TimeUnit.SECONDS);
			}
			if (dlm != null) {
				dlm.close();
			}
			if (namespace != null) {
				namespace.close();
			}
		}
	}

	@Test
	public void read() throws Exception {
		DistributedLogManager dlm = null;
		Namespace namespace = null;
		try {
			URI uri = URI.create("distributedlog://127.0.0.1:7000/messaging/my_namespace");
			DistributedLogConfiguration conf = new DistributedLogConfiguration();
			conf.setImmediateFlushEnabled(true);
			conf.setOutputBufferSize(0);
			conf.setPeriodicFlushFrequencyMilliSeconds(0);
			conf.setLockTimeout(DistributedLogConstants.LOCK_IMMEDIATE);

			namespace = NamespaceBuilder.newBuilder()
				.conf(conf)
				.uri(uri)
				.regionId(DistributedLogConstants.LOCAL_REGION_ID)
				.clientId("console-writer")
				.build();
			dlm = namespace.openLog("basic-stream-1");
			LogRecordWithDLSN record = dlm.getLastLogRecord();
			byte[] payload = record.getPayload();
			System.out.println(payload);
			System.out.println(new String(payload));
			DLSN dlsn = record.getDlsn();
			System.out.println(dlsn);
		} finally {
			if (dlm != null) {
				dlm.close();
			}
			if (namespace != null) {
				namespace.close();
			}
		}
	}
}
