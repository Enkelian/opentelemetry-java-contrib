/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.jfr.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.testing.assertj.MetricAssertions;
import io.opentelemetry.sdk.testing.assertj.MetricDataAssert;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Collection;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeAll;

public class AbstractMetricsTest {

  static SdkMeterProvider meterProvider;
  static InMemoryMetricReader metricReader;

  @BeforeAll
  static void initializeOpenTelemetry() {
    metricReader = InMemoryMetricReader.create();
    meterProvider = SdkMeterProvider.builder().registerMetricReader(metricReader).build();
    GlobalOpenTelemetry.set(OpenTelemetrySdk.builder().setMeterProvider(meterProvider).build());
    JfrMetrics.enable(meterProvider);
  }

  @SafeVarargs
  protected final void waitAndAssertMetrics(Consumer<MetricDataAssert>... assertions) {
    await()
        .untilAsserted(
            () -> {
              Collection<MetricData> metrics = metricReader.collectAllMetrics();

              assertThat(metrics).isNotEmpty();

              for (Consumer<MetricDataAssert> assertion : assertions) {
                assertThat(metrics)
                    .anySatisfy(metric -> assertion.accept(MetricAssertions.assertThat(metric)));
              }
            });
  }
}
