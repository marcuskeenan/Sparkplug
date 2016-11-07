/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2016 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.cirruslink.sparkplug.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.cirruslink.sparkplug.SparkplugException;
import com.cirruslink.sparkplug.SparkplugInvalidTypeException;
import com.cirruslink.sparkplug.message.PayloadDecoder;
import com.cirruslink.sparkplug.message.SparkplugBPayloadDecoder;
import com.cirruslink.sparkplug.message.SparkplugBPayloadEncoder;
import com.cirruslink.sparkplug.message.model.*;
import com.cirruslink.sparkplug.message.model.DataSet.DataSetBuilder;
import com.cirruslink.sparkplug.message.model.MetaData.MetaDataBuilder;
import com.cirruslink.sparkplug.message.model.Metric.MetricBuilder;
import com.cirruslink.sparkplug.message.model.PropertySet.PropertySetBuilder;
import com.cirruslink.sparkplug.message.model.Row.RowBuilder;
import com.cirruslink.sparkplug.message.model.Template.TemplateBuilder;

public class SparkplugTest {
	
	@BeforeClass
	public void beforeClass() {
	    Logger rootLogger = Logger.getRootLogger();
	    rootLogger.setLevel(Level.ALL);
	    rootLogger.addAppender(new ConsoleAppender(new PatternLayout("%-6r [%p] %c - %m%n")));
	}
	
    @DataProvider
	public Object[][] metricData() throws Exception { 
		return new Object[][] { 
			{ "TestByteObject", MetricDataType.Int8, new Byte((byte)123), null},
			{ "TestByte", MetricDataType.Int8, (byte)123, null},
			{ "TestShortObject", MetricDataType.Int16, new Short((short)12345), null},
			{ "TestShort", MetricDataType.Int16, (short)12345, null},
			{ "TestIntObject", MetricDataType.Int32, new Integer(1234567890), null},
			{ "TestInt", MetricDataType.Int32, 1234567890, null},
			{ "TestLongObject", MetricDataType.Int64, new Long(12345679000L), null},
			{ "TestLong", MetricDataType.Int64, 12345679000L, null}, 
			{ "TestUnsignedByte", MetricDataType.UInt8, new Short((short)123), null},
			{ "TestUnsignedShort", MetricDataType.UInt16, 12345, null},
			{ "TestUnsignedInt", MetricDataType.UInt32,  new Long(1234567890), null},
			{ "TestUnsignedLong", MetricDataType.UInt64, BigInteger.valueOf(12345679000L), null},
			{ "TestFloatObject", MetricDataType.Float, new Float(1.11111111111111111e+30f), null},
			{ "TestFloat", MetricDataType.Float, 1.11111111111111111e+30f, null},
			{ "TestDoubleObject", MetricDataType.Double, new Double(1.11111111111111111e+300d), null},
			{ "TestDouble", MetricDataType.Double, 1.11111111111111111e+300d, null},
			{ "TestBooleanObject", MetricDataType.Boolean, new Boolean(true), null},
			{ "TestBoolean", MetricDataType.Boolean, true, null},
			{ "TestString", MetricDataType.String, "TEST_STRING", null},
			{ "TestDateTime", MetricDataType.DateTime, new Date(), null},
			{ "TestText", MetricDataType.Text, "TEST_TEXT", null},
			{ "TestUUID", MetricDataType.UUID, "915cac68-a20e-11e6-80f5-76304dec7eb7", null},
			{ "TestBytes", MetricDataType.Bytes, new byte[]{0x0, 0x1, 0x2, 0x3, 0x4}, null},
			{ "TestFile", MetricDataType.File, new File("/tmp/.testfile", new byte[]{0x0, 0x1, 0x2, 0x3, 0x4}), 
					new MetaDataBuilder().fileType("bin").fileName("/tmp/.testfile").createMetaData()},
			{ "TestDataSet", MetricDataType.DataSet, new DataSetBuilder(3)
					.addColumnName("Booleans")
					.addColumnName("Int32s")
					.addColumnName("Floats")
					.addType(DataSetDataType.Boolean)
					.addType(DataSetDataType.Int32)
					.addType(DataSetDataType.Float)
					.addRow(new RowBuilder()
							.addValue(new Value<Boolean>(DataSetDataType.Boolean, false))
							.addValue(new Value<Integer>(DataSetDataType.Int32, 1))
							.addValue(new Value<Float>(DataSetDataType.Float, 1.1F))
							.createRow())
					.addRow(new RowBuilder()
							.addValue(new Value<Boolean>(DataSetDataType.Boolean, true))
							.addValue(new Value<Integer>(DataSetDataType.Int32, 2))
							.addValue(new Value<Float>(DataSetDataType.Float, 1.2F))
							.createRow())
					.addRow(new RowBuilder()
							.addValue(new Value<Boolean>(DataSetDataType.Boolean, false))
							.addValue(new Value<Integer>(DataSetDataType.Int32, 3))
							.addValue(new Value<Float>(DataSetDataType.Float, 1.3F))
							.createRow())
					.createDataSet()
					, null},
			{ "TestTemplateDef", MetricDataType.Template, new TemplateBuilder("TestTemplateDef")
					.version("v1.0")
					.templateRef(null)
					.definition(true)
					.addParameter(new Parameter("BoolParam", ParameterDataType.Boolean, true))
					.addParameter(new Parameter("IntParam", ParameterDataType.Int32, 12345678))
					.addParameter(new Parameter("DateParam", ParameterDataType.DateTime, new Date()))
					.addMetric(new MetricBuilder("TemplateMetric1", MetricDataType.Boolean, true)
							.createMetric())
					.addMetric(new MetricBuilder("TemplateMetric2", MetricDataType.Int32, 1234567890)
							.createMetric())
					.addMetric(new MetricBuilder("TemplateMetric3", MetricDataType.String, "TEST_STRING")
							.propertySet(new PropertySetBuilder()
									.addProperty("prop1a", new PropertyValue(PropertyDataType.Float, 1.23F))
									.addProperty("prop1b", new PropertyValue(PropertyDataType.Float, new Float(1.23)))
									.addProperty("prop2", new PropertyValue(PropertyDataType.DateTime, new Date()))
									.addProperty("prop3", new PropertyValue(PropertyDataType.Text, "PROP3_TEXT"))
									.addProperty("prop4", new PropertyValue(PropertyDataType.String, null))
									.createPropertySet())
							.createMetric())
					.createTemplate(), 
					null},
			{ "TestTemplateInst", MetricDataType.Template, new TemplateBuilder("TestTemplateInst")
					.version("v1.0")
					.templateRef("TestTemplateDef")
					.definition(true)
					.addParameter(new Parameter("BoolParam", ParameterDataType.Boolean, true))
					.addParameter(new Parameter("IntParam", ParameterDataType.Int32, 1234567890))
					.addParameter(new Parameter("DateParam", ParameterDataType.DateTime, new Date()))
					.addMetric(new MetricBuilder("TemplateMetric1", MetricDataType.Boolean, true)
							.createMetric())
					.addMetric(new MetricBuilder("TemplateMetric2", MetricDataType.Int32, 1234567890)
							.createMetric())
					.addMetric(new MetricBuilder("TemplateMetric3", MetricDataType.String, "TEST_STRING")
							.createMetric())
					.createTemplate(), 
					null}
		};
	}
    
    @DataProvider
	public Object[][] invalidParamterTypeData() throws Exception { 
		return new Object[][] { 
			{ "Param1", ParameterDataType.Boolean, 12345 },
			{ "Param2", ParameterDataType.Int8, true },
			{ "Param3", ParameterDataType.Int16, "123" },
			{ "Param4", ParameterDataType.Int32, true },
			{ "Param5", ParameterDataType.Int64, new Date() },
			{ "Param6", ParameterDataType.DateTime, 12345 },
			{ "Param7", ParameterDataType.Text, 12345 },
			{ "Param8", ParameterDataType.String, 12345 },
		};
	}
    
    @DataProvider
	public Object[][] invalidMetricDataType() throws Exception { 
		return new Object[][] { 
			{ "TestByteObject", MetricDataType.Int8, new Short((short)123), null},
			{ "TestByte", MetricDataType.Int8, new Long(12345679000L), null},
			{ "TestShortObject", MetricDataType.Int16, new Float(12345), null},
			{ "TestBooleanObject", MetricDataType.Boolean,  new Double(1.11111111111111111e+300d), null},
			{ "TestBoolean", MetricDataType.Boolean, 123, null},
			{ "TestString", MetricDataType.String, true, null},
			{ "TestDateTime", MetricDataType.DateTime, 1234567, null},
			{ "TestUUID", MetricDataType.UUID, 998877, null},
			{ "TestBytes", MetricDataType.Bytes, new int[]{1, 2, 3, 4, 5}, null}
		};
	}

    @Test
    public void testEnDeCode() throws SparkplugInvalidTypeException {
    	Date currentTime = new Date();
    	SparkplugBPayload sparkplugBPayload = new SparkplugBPayload();
    	sparkplugBPayload.setTimestamp(currentTime);
    	sparkplugBPayload.setSeq(0);
    	sparkplugBPayload.setBody("Hello".getBytes());

    	// Create MetaData
    	MetaData metaData = new MetaDataBuilder()
    			.contentType("none")
    			.size(12L)
    			.seq(0L)
    			.fileName("none")
    			.fileType("none")
    			.md5("none")
    			.description("none").createMetaData();
    	
    	// Create one metric
    	sparkplugBPayload.addMetric(new MetricBuilder("Name", MetricDataType.Int8, (byte)65)
    			.alias(0L)
    			.timestamp(currentTime)
    			.isHistorical(false)
    			.metaData(metaData)
    			.createMetric());

    	// Create null metric
    	sparkplugBPayload.addMetric(new MetricBuilder("Null", MetricDataType.String, null)
    			.alias(0L)
    			.timestamp(currentTime)
    			.isHistorical(false)
    			.metaData(metaData)
    			.createMetric());

    	// Encode
    	SparkplugBPayloadEncoder encoder = new SparkplugBPayloadEncoder();
    	byte[] bytes = null;
    	try {
    		bytes = encoder.getBytes(sparkplugBPayload);
    	} catch (IOException e) {
    		e.printStackTrace();
    		fail(e.getMessage());
    	}

    	// Decode
    	PayloadDecoder<SparkplugBPayload> decoder = new SparkplugBPayloadDecoder();
    	SparkplugBPayload decodedPayload = null;
    	try {
    		decodedPayload = decoder.buildFromByteArray(bytes);
    	} catch (Exception e) {
    		e.printStackTrace();
    		fail(e.getMessage());
    	}

    	// SparkplugBPayload checks
    	assertThat(currentTime).isEqualTo(decodedPayload.getTimestamp());
    	assertThat(0L).isEqualTo(decodedPayload.getSeq());
    	assertThat(Arrays.equals("Hello".getBytes(), decodedPayload.getBody())).isTrue();

    	// Test the Metric
    	assertThat(2).isEqualTo(decodedPayload.getMetrics().size());
    	Metric decodedMetric = decodedPayload.getMetrics().get(0);
    	assertThat("Name").isEqualTo(decodedMetric.getName());
    	assertThat(new Long(0)).isEqualTo(decodedMetric.getAlias());
    	assertThat(currentTime).isEqualTo(decodedMetric.getTimestamp());
    	assertThat(MetricDataType.Int8).isEqualTo(decodedMetric.getDataType());
    	assertThat(Boolean.FALSE).isEqualTo(decodedMetric.isHistorical());
    	assertThat((byte)65).isEqualTo(decodedMetric.getValue());
    	assertThat(decodedMetric.getMetaData()).isNotNull();
    	// Test the MetaData
    	MetaData decodedMetaData = decodedMetric.getMetaData();
    	assertThat(metaData).isEqualTo(decodedMetric.getMetaData());
    	assertThat("none").isEqualTo(decodedMetaData.getContentType());
    	assertThat(12L).isEqualTo(decodedMetaData.getSize());
    	assertThat(0L).isEqualTo(decodedMetaData.getSeq());
    	assertThat("none").isEqualTo(decodedMetaData.getFileName());
    	assertThat("none").isEqualTo(decodedMetaData.getFileType());
    	assertThat("none").isEqualTo(decodedMetaData.getMd5());
    	assertThat("none").isEqualTo(decodedMetaData.getDescription());
    }
	
	@Test(dataProvider = "metricData")
	public void testValidMetricPayload(String name, MetricDataType type, Object value, MetaData metaData) 
			throws SparkplugException {
		testMetricPayload(name, type, value, metaData);
	}
	
	@Test(dataProvider = "invalidMetricDataType", expectedExceptions = SparkplugInvalidTypeException.class)
	public void testInvalidMetricDataType(String name, MetricDataType type, Object value, MetaData metaData) 
			throws SparkplugException {
		testMetricPayload(name, type, value, metaData);
	}
	
	@Test(dataProvider = "invalidParamterTypeData", expectedExceptions = SparkplugInvalidTypeException.class)
	public void testInvalidParameterDataType(String name, ParameterDataType type, Object value) 
			throws SparkplugInvalidTypeException {
		new Parameter(name, type, value);
	}
	
	public void testMetricPayload(String name, MetricDataType type, Object value, MetaData metaData) 
			throws SparkplugException{
		try {
			// Now test a minimal payload
			Date currentTime = new Date();
			SparkplugBPayload sparkplugBPayload = new SparkplugBPayload();
			sparkplugBPayload.setTimestamp(currentTime);
			
			// Create one metric
			sparkplugBPayload.addMetric(new MetricBuilder(name, type, value)
					.metaData(metaData)
					.createMetric());
			
			// Encode
			SparkplugBPayloadEncoder encoder = new SparkplugBPayloadEncoder();
			byte[] bytes = encoder.getBytes(sparkplugBPayload);
			
			// Decode
			PayloadDecoder<SparkplugBPayload> decoder = new SparkplugBPayloadDecoder();
			SparkplugBPayload decodedPayload = decoder.buildFromByteArray(bytes);
			
			// SparkplugBPayload checks
			assertThat(currentTime).isEqualTo(decodedPayload.getTimestamp());
			assertThat(-1L).isEqualTo(decodedPayload.getSeq());
			assertThat(decodedPayload.getBody()).isNull();
			
			// Metric checks
			assertThat(1).isEqualTo(decodedPayload.getMetrics().size());
			
			doMetricTests(new MetricBuilder(name, type, value).metaData(metaData).createMetric(), 
					decodedPayload.getMetrics().get(0));
		} catch (SparkplugException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	private void doMetricTests(Metric metric, Metric decodedMetric) throws Exception {
		String name = metric.getName(); 
		MetricDataType type = metric.getDataType(); 
		Object value = metric.getValue();
		MetaData metaData = metric.getMetaData();
		PropertySet propertySet = metric.getPropertySet();
		
		assertThat(name).isEqualTo(decodedMetric.getName());
		assertThat(type).isEqualTo(decodedMetric.getDataType());
		assertThat(Boolean.FALSE).isEqualTo(decodedMetric.isHistorical());
		
		// Test PropertySet
		if (propertySet != null) {
			Map<String, PropertyValue> map = propertySet.getPropertyMap();
			Map<String, PropertyValue> decodedMap = decodedMetric.getPropertySet().getPropertyMap();
			assertThat(map.size()).isEqualTo(decodedMap.size());
			for (String key : map.keySet()) {
				assertThat(map.get(key)).isEqualTo(decodedMap.get(key));
			}
		}

		// Test the value
		switch (type) {
			case Bytes:
				compareBytes((byte []) value, (byte []) decodedMetric.getValue());
				assertThat(decodedMetric.getMetaData()).isNull();
				break;
			case File:
				File someFile = (File) value;
				File decodedFile = (File) decodedMetric.getValue();
				compareBytes(someFile.getBytes(), decodedFile.getBytes());
				assertThat(someFile.getFileName()).isEqualTo(decodedFile.getFileName());
				assertThat(decodedMetric.getMetaData()).isNotNull();
				assertThat(metaData.getFileName()).isEqualTo(decodedMetric.getMetaData().getFileName());
				assertThat(metaData.isMultiPart()).isEqualTo(decodedMetric.getMetaData().isMultiPart());
				assertThat(metaData.getFileType()).isEqualTo(decodedMetric.getMetaData().getFileType());
				break;
			case DataSet:
				// Tests for DataSets
				DataSet dataSet = (DataSet) value;
				DataSet decodedDataSet = (DataSet) decodedMetric.getValue();
				List<String> columnNames = dataSet.getColumnNames();
				List<String> decodedColumnNames = decodedDataSet.getColumnNames();
				List<DataSetDataType> types = dataSet.getTypes();
				List<DataSetDataType> decodedTypes = decodedDataSet.getTypes();
				List<Row> rows = dataSet.getRows();
				List<Row> decodedRows = decodedDataSet.getRows();
				assertThat(dataSet.getNumOfColumns()).isEqualTo(decodedDataSet.getNumOfColumns());
				// Test Columns
				for (int i = 0; i < columnNames.size(); i++) {
					assertThat(columnNames.get(i)).isEqualTo(decodedColumnNames.get(i));
				}
				// Test Types
				for (int i = 0; i < types.size(); i++) {
					assertThat(types.get(i)).isEqualTo(decodedTypes.get(i));
				}
				// Test Rows
				for (int i = 0; i < rows.size(); i++) {
					List<Value<?>> values = rows.get(i).getValues();
					List<Value<?>> decodedValues = decodedRows.get(i).getValues();
					assertThat(values.size()).isEqualTo(decodedValues.size());
					for (int j = 0; j < values.size(); j++) {
						Value<?> rowValue = values.get(j);
						Value<?> decodedValue = decodedValues.get(j);
						assertThat(rowValue.getType()).isEqualTo(decodedValue.getType());
						assertThat(rowValue.getValue()).isEqualTo(decodedValue.getValue());
					}
				}
				break;
			case Template:
				// Tests for Templates
				Template template = (Template) value;
				Template decodedTemplate = (Template) decodedMetric.getValue();
				List<Parameter> parameters = template.getParameters();
				List<Parameter> decodedParameters = decodedTemplate.getParameters();
				List<Metric> metrics = template.getMetrics();
				List<Metric> decodedMetrics = decodedTemplate.getMetrics();
				// Test Parameters
				assertThat(parameters.size()).isEqualTo(decodedParameters.size());
				for (int i = 0; i < parameters.size(); i++) {
					assertThat(parameters.get(i)).isEqualTo(decodedParameters.get(i));
				}
				// Test Metrics
				for (int i = 0; i < metrics.size(); i++) {
					doMetricTests(metrics.get(i), decodedMetrics.get(i));
				}
				break;
			default:
				// Tests for all other types
				assertThat(value).isEqualTo(decodedMetric.getValue());
				assertThat(decodedMetric.getMetaData()).isNull();
		}
	}
	
	private void compareBytes(byte [] bytes1, byte [] bytes2) {
		assertThat(bytes1.length).isEqualTo(bytes2.length);
		for (int i = 0; i < bytes1.length; i++) {
			assertThat(bytes1[i]).isEqualTo(bytes2[i]);
		}
	}
}
