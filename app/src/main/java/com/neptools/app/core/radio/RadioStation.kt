package com.neptools.app.core.radio

import java.io.Serializable

data class RadioStation(
    val id: String,
    val nameNp: String,
    val nameEn: String,
    val frequency: String,
    val location: String,
    val category: String, // "national", "news", "music"
    val streamUrl: String,
    val fallbackUrl: String? = null
) : Serializable

object RadioStations {
    val all = listOf(
        RadioStation("kantipur", "रेडियो कान्तिपुर", "Radio Kantipur", "96.1 MHz", "Lalitpur", "national", "https://radio-broadcast.ekantipur.com/stream", "http://broadcast.radiokantipur.com:7014/stream"),
        RadioStation("radionepal", "रेडियो नेपाल", "Radio Nepal", "100.0 MHz", "Singha Durbar, Kathmandu", "national", "https://stream1.radionepal.gov.np/live/"),
        RadioStation("ujyaalo", "उज्यालो ९० नेटवर्क", "Ujyaalo 90", "90.0 MHz", "Kathmandu", "news", "http://stream.zenolive.com/wtuvp08xq1duv", "https://stream.zeno.fm/0r0xa792kwzuv"),
        RadioStation("hitsfm", "हिट्स एफएम", "Hits FM", "91.2 MHz", "Kathmandu", "music", "https://usa15.fastcast4u.com/proxy/hitsfm912?mp=/1"),
        RadioStation("bbcnepali", "बीबीसी नेपाली", "BBC Nepali", "103 MHz", "London / Kathmandu", "news", "https://stream.live.vc.bbcmedia.co.uk/bbc_nepali_radio"),
        RadioStation("imagefm", "इमेज एफएम", "Image FM", "97.9 MHz", "Kathmandu", "music", "https://stream.zeno.fm/0r0xa792kwzuv"),
        RadioStation("kalikafm", "कालिका एफएम", "Kalika FM", "95.2 MHz", "Bharatpur, Chitwan", "national", "http://kalika-stream.softnep.com:7740/;"),
        RadioStation("capitalfm", "क्यापिटल एफएम", "Capital FM", "92.4 MHz", "Kathmandu", "national", "http://streaming.softnep.net:8037/;"),
        RadioStation("thaha", "रेडियो थाहा सञ्चार", "Radio Thaha Sanchar", "99.4 MHz", "Kathmandu / Hetauda", "news", "https://streaming.softnep.net:10988/;stream.nsv&type=mp3"),
        RadioStation("audio", "रेडियो अडियो", "Radio Audio", "106.3 MHz", "Kathmandu", "music", "https://stream.zeno.fm/fvrx47wpg0quv"),
        RadioStation("sagarmatha", "रेडियो सगरमाथा", "Radio Sagarmatha", "102.4 MHz", "Lalitpur", "national", "https://eu1.fastcast4u.com/proxy/radiosag?mp=/stream/1/")
    )
}
