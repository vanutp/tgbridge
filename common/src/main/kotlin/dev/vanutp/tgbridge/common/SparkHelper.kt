package dev.vanutp.tgbridge.common

import me.lucko.spark.api.Spark
import me.lucko.spark.api.SparkProvider
import me.lucko.spark.api.statistic.StatisticWindow

class SparkHelper private constructor(private val spark: Spark) {
    companion object {
        fun createOrNull(): SparkHelper? {
            val spark = try {
                SparkProvider.get()
            } catch (ignore: NoClassDefFoundError) {
                return null
            } catch (ignore: IllegalStateException) {
                TelegramBridge.INSTANCE.logger.warn("Spark is present but not enabled")
                return null
            }
            return SparkHelper(spark)
        }
    }

    fun getPlaceholders(): Array<Pair<String, String>>? {
        val tpsDurations = mapOf(
            "tps5s" to StatisticWindow.TicksPerSecond.SECONDS_5,
            "tps10s" to StatisticWindow.TicksPerSecond.SECONDS_10,
            "tps1m" to StatisticWindow.TicksPerSecond.MINUTES_1,
            "tps5m" to StatisticWindow.TicksPerSecond.MINUTES_5,
            "tps15m" to StatisticWindow.TicksPerSecond.MINUTES_15,
        )
        val tps = spark.tps() ?: return null
        val tpsPlaceholders =tpsDurations.map {
            it.key to "%.1f".format(tps.poll(it.value))
        }

        val msptDurations = mapOf(
            "mspt10sAvg" to StatisticWindow.MillisPerTick.SECONDS_10,
            "mspt1mAvg" to StatisticWindow.MillisPerTick.MINUTES_1,
            "mspt5mAvg" to StatisticWindow.MillisPerTick.MINUTES_5,
        )
        val mspt = spark.mspt() ?: return null
        val msptPlaceholders =  msptDurations.map {
            it.key to "%.1f".format(mspt.poll(it.value).mean())
        }

        return (tpsPlaceholders + msptPlaceholders).toTypedArray()
    }
}
