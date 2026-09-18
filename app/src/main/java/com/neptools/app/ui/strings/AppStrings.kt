package com.neptools.app.ui.strings

import androidx.compose.runtime.Composable
import com.neptools.app.ui.theme.ThemePrefs

private val S: Map<String, Pair<String, String>> = mapOf(

    // nav
    "app_name" to ("नेपटूल्स" to "NepTools"),
    "nav_home" to ("गृह" to "Home"),
    "nav_patro" to ("पात्रो" to "Calendar"),
    "nav_tools" to ("टूल्स" to "Tools"),
    "nav_settings" to ("सेटिङ" to "Settings"),

    // common
    "back" to ("पछाडि" to "Back"),
    "all" to ("सबै" to "All"),
    "today" to ("आज" to "Today"),
    "soon" to ("आउँदैछ" to "Soon"),
    "result" to ("नतिजा" to "Result"),
    "year" to ("वर्ष" to "Year"),
    "month" to ("महिना" to "Month"),
    "day" to ("गते" to "Day"),
    "save" to ("सेभ गर्नुहोस्" to "Save"),

    // greetings
    "greet_morning" to ("शुभ प्रभात" to "Good morning"),
    "greet_day" to ("नमस्ते" to "Namaste"),
    "greet_evening" to ("शुभ साँझ" to "Good evening"),
    "greet_night" to ("शुभ रात्री" to "Good night"),

    // home
    "sunrise" to ("सूर्योदय" to "Sunrise"),
    "sunset" to ("सूर्यास्त" to "Sunset"),
    "rahu" to ("राहु काल" to "Rahu Kaal"),
    "utilities" to ("उपयोगिता" to "Utilities"),
    "tools_sub" to ("सेवा तथा टूल्स" to "Services & Tools"),
    "festival_today" to ("आज:" to "Today:"),
    "festival_on" to ("गते:" to "On the"),
    "no_festival" to ("आज कुनै पर्व छैन" to "No festival today"),
    "shukla" to ("शुक्ल पक्ष" to "Shukla Paksha"),
    "krishna" to ("कृष्ण पक्ष" to "Krishna Paksha"),
    "tithi_word" to ("तिथि" to "Tithi"),

    // home tile short labels
    "tile_license" to ("लाइसेन्स" to "License"),
    "tile_radio" to ("रेडियो" to "FM Radio"),
    "tile_gold" to ("सुनचाँदी" to "Gold & Silver"),
    "tile_kali" to ("कालिमाटी" to "Kalimati"),
    "tile_conv" to ("रूपान्तरण" to "Date Conv"),
    "tile_curr" to ("मुद्रा दर" to "Currency"),
    "tile_astro" to ("कुण्डली" to "Kundali"),
    "tile_tmpl" to ("निवेदन" to "Templates"),
    "tile_loan" to ("ऋण EMI" to "Loan EMI"),
    "tile_emg" to ("सम्पर्क" to "Emergency"),
    "tile_bill" to ("महसुल" to "Bill Calc"),
    "tile_fuel" to ("इन्धन" to "Fuel Price"),
    "tile_post" to ("हुलाक" to "Postal Code"),
    "tile_qr" to ("क्युआर" to "QR Code"),
    "tile_age" to ("उमेर" to "Age Calc"),

    // tools list
    "tools_hdr_daily" to ("दैनिक सेवाहरू" to "Daily Services"),
    "tools_hdr_citizen" to ("नागरिक सेवा" to "Citizen Services"),
    "tools_hdr_calc" to ("वित्तीय तथा क्यालकुलेटर" to "Finance & Calculators"),
    "tools_hdr_prod" to ("उत्पादकत्व तथा मिडिया" to "Productivity & Media"),
    "weather" to ("मौसम तथा पूर्वानुमान" to "Weather & Forecast"),
    "weather_sub" to ("प्रत्यक्ष तथा ७ दिने पूर्वानुमान" to "Live 7-day & hourly forecast"),
    "daily_rashifal" to ("दैनिक राशिफल" to "Daily Horoscope"),
    "rashifal_sub" to ("१२ वटै राशिको आजको फलादेश" to "Daily readings for 12 zodiac signs"),
    "astrology" to ("जन्म कुण्डली तथा ज्योतिष" to "Kundali & Astrology"),
    "astrology_sub" to ("लग्न कुण्डली, ग्रहदशा र गोचर" to "Birth chart, planetary positions & dasha"),
    "date_conv" to ("मिति रूपान्तरण" to "Date Converter"),
    "date_conv_sub" to ("बि.सं. र ई.सं. बीच रूपान्तरण" to "Convert dates between BS and AD"),
    "currency" to ("विदेशी मुद्रा विनिमय दर" to "Forex Currency Rates"),
    "currency_sub" to ("राष्ट्र बैंकको दैनिक विनिमय दर" to "Live Nepal Rastra Bank forex rates"),
    "age_calc" to ("उमेर क्यालकुलेटर" to "Age Calculator"),
    "age_sub" to ("वर्ष, महिना, दिन तथा जन्मदिन" to "Exact age in years, months & days"),
    "ocr" to ("तस्बिरबाट अक्षर पहिचान" to "OCR Text Scanner"),
    "ocr_sub" to ("तस्बिर स्क्यान गरी पाठ निकाल्नुहोस्" to "Extract text from photos"),
    "voice" to ("नेपाली भ्वाइस नोट" to "Voice Notes"),
    "voice_sub" to ("आवाजबाट नेपालीमा टाइप गर्नुहोस्" to "Speech to text in Nepali"),
    "tool_radio_t" to ("लाइभ एफएम रेडियो" to "Live FM Radio"),
    "tool_radio_s" to ("नेपालभरिका चर्चित रेडियो तथा समाचार" to "Popular Nepali radio & news streams"),
    "tool_emg_t" to ("आपतकालीन सम्पर्क" to "Emergency Helplines"),
    "tool_emg_s" to ("प्रहरी, एम्बुलेन्स, दमकल र हटलाइनहरू" to "Police, Ambulance, Fire & Helplines"),
    "tool_bill_t" to ("महसुल क्यालकुलेटर" to "Bill Calculator"),
    "tool_bill_s" to ("विद्युत (NEA) र खानेपानी बिल" to "NEA Electricity & Water Tariff"),
    "tool_fuel_t" to ("पेट्रोलियम भाउ" to "Fuel Price Tracker"),
    "tool_fuel_s" to ("पेट्रोल, डिजेल र ग्यास मूल्य तथा क्यालकुलेटर" to "Petrol, Diesel & LPG rates + trip cost"),
    "tool_post_t" to ("हुलाक कोड (Zip Code)" to "Postal / Zip Codes"),
    "tool_post_s" to ("७७ जिल्लाका हुलाक कार्यालयका कोडहरू" to "77 Districts postal code directory"),
    "tool_qr_t" to ("क्युआर कोड जेनेरेटर" to "QR Code Generator"),
    "tool_qr_s" to ("वेबसाइट, पाठ, वाइफाइ र eSewa QR बनाउनुहोस्" to "Generate custom QR for Web, WiFi, Text"),
    "tool_gold_t" to ("सुन–चाँदीको भाउ" to "Gold & Silver Rates"),
    "tool_gold_s" to ("दैनिक सुनचाँदी दर तथा गहना बिल हिसाब" to "Live daily rates + jewelry bill estimator"),
    "tool_kali_t" to ("कालिमाटी तरकारी बजार" to "Kalimati Market"),
    "tool_kali_s" to ("दैनिक तरकारी तथा फलफूलको थोक/खुद्रा भाउ" to "Daily fruits & vegetable market prices"),
    "tool_tmpl_t" to ("सरकारी निवेदन ढाँचा" to "Application Templates"),
    "tool_tmpl_s" to ("नागरिकता, जग्गा, वडा, बिदा तथा कानुनी निवेदन" to "Official verified citizen letter templates"),
    "tool_loan_t" to ("बैंक ऋण EMI तथा मुद्दती" to "Loan EMI & FD Calculator"),
    "tool_loan_s" to ("मासिक किस्ता (EMI) तथा मुद्दती निक्षेप प्रतिफल" to "Calculate monthly EMI & FD returns"),

    // calendar
    "month_head" to ("चल्ती महिना" to "This month"),
    "month_sub" to ("दिन थिच्नुहोस् — विवरण खुल्छ" to "Tap any day for detail"),
    "fest_this_month" to ("यस महिनाका पर्वहरू" to "Festivals this month"),
    "fest_sub" to ("सार्वजनिक बिदा तथा पर्वहरू" to "Public holidays & festivals"),
    "legend_fest" to ("पर्व / बिदा" to "Festival / holiday"),
    "legend_today" to ("आज" to "Today"),
    "cal_empty_fest" to ("यस महिनामा दर्ता पर्व छैन" to "No registered festivals this month"),

    // day detail
    "day_detail" to ("दिन विवरण" to "Day Details"),
    "paksha" to ("पक्ष" to "Paksha"),
    "nakshatra" to ("नक्षत्र" to "Nakshatra"),
    "yoga" to ("योग" to "Yoga"),
    "moonrise" to ("चन्द्रोदय" to "Moonrise"),
    "set_reminder" to ("यो दिनको सम्झना राख्नुहोस्" to "Remind me about this day"),
    "reminder_ok" to ("सम्झना सेट भयो" to "Reminder set"),
    "reminder_fail" to ("सेट गर्न सकिएन" to "Couldn't set reminder"),
    "public_hol" to ("सार्वजनिक बिदा" to "Public holiday"),
    "main_fest" to ("मुख्य पर्व" to "Main festival"),

    // converter
    "conv_title" to ("मिति परिवर्तन" to "Date Converter"),
    "bs_era" to ("बि.सं. (नेपाली पात्रो)" to "B.S. (Nepali Era)"),
    "ad_era" to ("ई.सं. (अंग्रेजी पात्रो)" to "A.D. (Gregorian)"),
    "bs_year_l" to ("वर्ष Year" to "Year"),
    "bs_month_l" to ("महिना Month" to "Month"),
    "bs_day_l" to ("गते Day" to "Day"),
    "out_range" to ("— दायरा बाहिर —" to "— out of range —"),
    "result_res" to ("नतिजा" to "Result"),

    // currency
    "cur_title" to ("विदेशी मुद्रा विनिमय" to "Currency"),
    "amount" to ("रकम" to "Amount"),
    "from" to ("बाट" to "From"),
    "to" to ("मा" to "To"),
    "refresh_rates" to ("दर अपडेट गर्नुहोस्" to "Refresh rates"),
    "updating" to ("अपडेट हुँदै…" to "Updating…"),
    "no_cache" to ("कुनै क्यास दर छैन — इन्टरनेट जोड्नुहोस्" to "No cached rates — connect internet"),
    "stale_note" to ("पुरानो क्यास दर" to "Cached rates"),
    "rate_date" to ("दर मिति:" to "Rate date:"),
    "cur_source" to ("स्रोत: नेपाल राष्ट्र बैंक आधिकारिक विनिमय दर" to "Source: Nepal Rastra Bank official forex rates"),

    // age
    "age_title" to ("उमेर गणना" to "Age Calculator"),
    "dob_bs" to ("जन्म मिति (बि.सं.)" to "Date of Birth (B.S.)"),
    "your_age" to ("तपाईंको उमेर" to "Your age"),
    "yrs" to ("वर्ष" to "y"),
    "mo" to ("महिना" to "m"),
    "dd" to ("दिन" to "d"),
    "total_days" to ("कुल दिन" to "Total days"),
    "total_weeks" to ("कुल हप्ता" to "Total weeks"),
    "total_hours" to ("कुल घण्टा" to "Total hours"),
    "next_bday" to ("अर्को जन्मदिन" to "Next birthday"),
    "left_days" to ("बाँकी दिन" to "days left"),

    // rashifal
    "rashi_title" to ("राशिफल" to "Horoscope"),
    "today_result" to ("आजको फलाफल" to "Today's reading"),
    "love" to ("प्रेम" to "Love"),
    "work" to ("करियर" to "Work"),
    "health" to ("स्वास्थ्य" to "Health"),
    "luck" to ("भाग्य" to "Luck"),
    "lucky_color" to ("शुभ रङ" to "Lucky colour"),
    "lucky_num" to ("शुभ अङ्क" to "Lucky number"),
    "lucky_time" to ("शुभ समय" to "Lucky time"),

    // settings
    "settings" to ("सेटिङ" to "Settings"),
    "sec_prefs" to ("प्राथमिकता" to "PREFERENCES"),
    "language" to ("भाषा" to "Language"),
    "lang_sub" to ("नेपाली वा English" to "Nepali or English"),
    "np_digits_t" to ("नेपाली अङ्क" to "Nepali numerals"),
    "np_digits_s" to ("१२३ वा 123 — पूरा एपमा" to "१२३ vs 123 app-wide"),
    "dark_t" to ("अँध्यारो मोड" to "Dark mode"),
    "dark_s" to ("Ink Night theme" to "Ink Night theme"),
    "sec_data" to ("डाटा तथा गोपनीयता" to "DATA & PRIVACY"),
    "data_cal" to ("क्यालेन्डर" to "Calendar"),
    "data_cal_v" to ("बि.सं. १९७५–२०९९" to "B.S. 1975–2099 offline"),
    "data_panch" to ("पञ्चाङ्ग" to "Panchang"),
    "data_panch_v" to ("अन-डिभाइस गणना" to "computed on-device"),
    "data_rate" to ("विनिमय दर" to "Currency rates"),
    "privacy" to ("गोपनीयता" to "Privacy"),
    "privacy_v" to ("कुनै account छैन · सबै डाटा फोनमै सुरक्षित" to "No account required · 100% private"),
    "ai_sec" to ("AI सुविधाहरू (आउँदैछ)" to "AI Features (Coming)"),
    "ai_note" to ("Devanagari OCR र नेपाली voice notes अर्को अपडेटमा।" to "Devanagari OCR & Nepali voice notes in next update."),

    // astrology hub
    "vedic" to ("ज्योतिष" to "Vedic"),
    "birth_details" to ("जन्म विवरण" to "Birth Details"),
    "birth_need" to ("कुण्डली बनाउन समय, मिति र ठेगाना चाहिन्छ।" to "Time, date & place are needed to build your chart."),
    "edit_birth" to ("जन्म विवरण भर्नुहोस्" to "Enter birth details"),
    "edit_birth2" to ("विवरण सच्याउनुहोस्" to "Edit birth details"),
    "overview" to ("OVERVIEW" to "OVERVIEW"),
    "details_hdr" to ("DETAILS" to "DETAILS"),
    "an_today" to ("आजको विश्लेषण" to "Today's Analysis"),
    "an_today_s" to ("Scores · Timeline · Today" to "Scores · Timeline · Today"),
    "an_chart" to ("जन्म कुण्डली" to "Birth Chart"),
    "an_chart_s" to ("Chart · Planets · Nakshatra" to "Chart · Planets · Nakshatra"),
    "an_dasha" to ("विंशोत्तरी दशा" to "Vimshottari Dasha"),
    "an_dasha_s" to ("Maha · Antar · Pratyantar" to "Maha · Antar · Pratyantar"),
    "an_gochar" to ("गोचर" to "Transits"),
    "an_gochar_s" to ("Live transits vs natal" to "Live transits vs natal"),
    "computing" to ("ग्रहगणना हुँदैछ…" to "Calculating planetary positions…"),
    "birth_missing" to ("अझै सेट छैन — पहिले भर्नुहोस्।" to "Not set yet — please fill in."),
    "utc" to ("UTC" to "UTC"),

    // birth form
    "bf_title" to ("जन्म विवरण" to "Birth Details"),
    "dob_ad" to ("जन्म मिति (AD)" to "Date of Birth"),
    "dob_hint" to ("YYYY-MM-DD" to "YYYY-MM-DD"),
    "tob" to ("जन्म समय (24h)" to "Time of Birth"),
    "lat" to ("अक्षांश Lat" to "Latitude"),
    "lon" to ("देशान्तर Lon" to "Longitude"),
    "tz" to ("TZ offset (hrs)" to "TZ offset (hrs)"),
    "tz_hint" to ("नेपाल = 5.75" to "Nepal = 5.75"),
    "place_hdr" to ("ठेगाना" to "Place / Location"),
    "search_ph" to ("जिल्ला / शहर / गाउँपालिका" to "Search district, city or municipality"),
    "change_tap" to ("ट्याप गरेर परिवर्तन" to "Tap to change"),
    "not_found" to ("कुनै ठेगाना भेटिएन — तल lat/lon हातले हाल्नुहोस्" to "No match — enter lat/lon manually below"),
    "err_date" to ("मिति YYYY-MM-DD ढाँचामा होस्" to "Date must be YYYY-MM-DD"),
    "err_time" to ("समय HH:MM ढाँचामा होस्" to "Time must be HH:MM"),
    "err_num" to ("Lat/Lon/TZ संख्या हुनुपर्छ" to "Lat/Lon/TZ must be numbers"),
    "uncertain" to ("जन्म समय अनिश्चित छ" to "Birth time uncertain"),
    "gen_kundali" to ("कुण्डली बनाउनुहोस्" to "GENERATE KUNDALI"),
    "custom" to ("Custom" to "Custom"),

    // kundali
    "kd_title" to ("जन्म कुण्डली" to "Birth Chart"),
    "lagna" to ("लग्न" to "Ascendant"),
    "moon_sign" to ("जन्म राशि" to "Moon sign"),
    "janma_nak" to ("जन्म नक्षत्र:" to "Birth star:"),
    "how_read" to ("यो कुण्डली कसरी पढ्ने?" to "How to read this chart?"),
    "chart_title" to ("कुण्डली चित्र" to "North Indian Chart"),
    "planets_ask" to ("ग्रहहरूको स्थिति" to "Planetary Positions"),
    "planets_sub" to ("हरेक ग्रहको स्थिति र त्यसको अर्थ" to "Each placement in simple words"),
    "house_w" to ("घर" to "House"),
    "pada" to ("चरण" to "pada"),
    "exact" to ("सटीकता" to "precision"),
    "retro" to ("वक्री" to "(R)"),
    "uncertain_warn" to ("जन्म समय अनिश्चित हुँदा लग्न र घर-गणना कम भरपर्दो हुन्छ।"
        to "With an uncertain birth time, the ascendant & houses may be less precise."),
    "empty_need_birth" to ("जन्म विवरण चाहिन्छ" to "Birth details needed"),
    "empty_need_birth_s" to ("पहिले मिति, समय र ठेगाना भर्नुहोस्" to "First add date, time & place"),
    "chart_house1" to ("· लग्न" to " · Asc"),

    // dasha
    "ds_title" to ("विंशोत्तरी दशा" to "Vimshottari Dasha"),
    "now_phase" to ("अहिलेको अवधि" to "Current phase"),
    "maha" to ("महादशा" to "Mahadasha"),
    "antar" to ("अन्तर्दशा" to "Antardasha"),
    "maha_seq" to ("महादशा क्रम" to "Mahadasha sequence"),
    "tag_current" to ("चालु" to "Now"),
    "antar_span" to ("अन्तर्दशा अवधि:" to "Antardasha:"),

    // gochar
    "go_title" to ("गोचर" to "Transits"),
    "go_sub" to ("चन्द्र राशिबाट हेरिएको अहिलेको ग्रहचाल" to "Current planetary transit from Moon sign"),
    "fav" to ("अनुकूल" to "Favorable"),
    "caution" to ("सावधानी" to "Needs care"),
    "go_disc" to ("परम्परागत चन्द्र-लग्न गोचर नियममा आधारित।"
        to "Based on classic Chandra-Lagna transit rules."),
    "moon_h" to ("चन्द्रबाट घर" to "from Moon"),

    // emergency
    "emg_title" to ("आपतकालीन सम्पर्क" to "Emergency Contacts"),
    "emg_search" to ("खोज्नुहोस् (प्रहरी, एम्बुलेन्स, अस्पताल, रगत…)" to "Search (police, ambulance, hospital, blood…)"),
    "emg_note" to ("सबै राष्ट्रिय हटलाइनहरू (१००, १०१, १०२, १०३, १०९८, ११४५) नेपालभित्र निःशुल्क डायल गर्न सकिन्छ।"
        to "All national hotlines (100, 101, 102, 103, 1098, 1145) are toll-free across Nepal."),
    "emg_cat_all" to ("सबै" to "All"),
    "emg_cat_sec" to ("सुरक्षा" to "Security"),
    "emg_cat_med" to ("स्वास्थ्य" to "Medical"),
    "emg_cat_res" to ("उद्धार" to "Rescue & Fire"),
    "emg_cat_soc" to ("सामाजिक" to "Social Help"),
    "emg_cat_bld" to ("रक्तसञ्चार" to "Blood Banks"),
    "emg_loc_title" to ("स्थान छान्नुहोस्" to "Select Location"),
    "emg_loc_gps" to ("GPS पत्ता लगाउनुहोस्" to "Detect via GPS"),
    "emg_loc_change" to ("परिवर्तन" to "Change"),
    "emg_loc_all" to ("सबै नेपाल (राष्ट्रिय)" to "All Nepal (National)"),
    "emg_loc_local_badge" to ("तपाईंको क्षेत्र" to "Your Area"),
    "emg_loc_locating" to ("GPS खोजी हुँदै…" to "Detecting GPS…"),
    "emg_loc_search_hint" to ("जिल्ला खोज्नुहोस्…" to "Search district…"),
    "emg_sync_title" to ("सम्पर्क अद्यावधिक" to "Sync Contacts"),
    "emg_sync_btn" to ("अद्यावधिक" to "Sync Now"),
    "emg_sync_success" to ("सम्पर्कहरू सफलतापूर्वक अद्यावधिक भए" to "Emergency contacts updated"),
    "emg_sync_latest" to ("सम्पर्कहरू पहिल्यै नवीनतम छन्" to "Contacts are up to date"),
    "emg_sync_failed" to ("सम्पर्क अद्यावधिक हुन सकेन" to "Failed to sync contacts"),

    // bill calc
    "bill_title" to ("महसुल क्यालकुलेटर" to "Bill Calculator"),
    "bill_elec" to ("विद्युत (NEA)" to "Electricity (NEA)"),
    "bill_water" to ("खानेपानी" to "Water (KUKL)"),
    "bill_units" to ("विद्युत खपत युनिट" to "Electricity Consumed Units"),
    "bill_units_hint" to ("मासिक युनिट (Units)" to "Monthly Units"),
    "bill_meter_cap" to ("मिटर क्षमता" to "Meter Capacity"),
    "bill_nea_sum" to ("नेपाल विद्युत प्राधिकरण (NEA) बिल विवरण" to "NEA Electricity Bill Summary"),
    "bill_min_chg" to ("न्यूनतम सेवा शुल्क" to "Minimum Service Charge"),
    "bill_eng_chg" to ("ऊर्जा शुल्क" to "Energy Charge"),
    "bill_total" to ("कुल महसुल" to "Total Payable"),
    "bill_disc" to ("७ दिनभित्र भुक्तानी गर्दा (२% छुट):" to "Within 7 days payment (2% rebate):"),
    "bill_fine" to ("३० दिनपछि भुक्तानी गर्दा (५% जरिवाना):" to "After 30 days payment (5% fine):"),
    "water_vol" to ("पानी खपत परिमाण" to "Water Consumption Volume"),
    "water_vol_hint" to ("खपत लिटर (Liters)" to "Consumed Liters"),
    "water_pipe" to ("पाइपको साइज" to "Pipe Size"),
    "water_kukl_sum" to ("काठमाडौं उपत्यका खानेपानी (KUKL) महसुल" to "KUKL Water Tariff Summary"),
    "water_min" to ("न्यूनतम महसुल" to "Minimum Tariff"),
    "water_extra" to ("थप पानी शुल्क" to "Additional Units Charge"),
    "water_sew" to ("ढल निकास शुल्क (५०%)" to "Sewage Fee (50%)"),

    // postal
    "post_title" to ("हुलाक कोड (Zip Code)" to "Postal / Zip Codes"),
    "post_search" to ("जिल्ला वा स्थान खोज्नुहोस्" to "Search district, city or municipality"),
    "post_found" to ("हुलाक कोडहरू भेटिए:" to "postal codes found:"),
    "post_source" to ("स्रोत: नेपाल सरकार, हुलाक सेवा विभाग" to "Source: Department of Postal Services, Nepal"),

    // fuel
    "fuel_title" to ("पेट्रोलियम भाउ" to "Fuel Prices"),
    "fuel_source_head" to ("नेपाल आयल निगम (NOC) आधिकारिक मूल्य:" to "Official NOC Retail Selling Prices:"),
    "fuel_region" to ("क्षेत्र / डिपो वर्ग:" to "Region Depot Category:"),
    "fuel_refresh" to ("दर अपडेट गर्नुहोस्" to "Refresh Live Rates"),
    "fuel_calc_head" to ("इन्धन क्यालकुलेटर" to "Fuel Calculator"),
    "fuel_tab_amt" to ("रकम ➔ लिटर" to "Amount ➔ Liters"),
    "fuel_tab_lit" to ("लिटर ➔ रकम" to "Liters ➔ Amount"),
    "fuel_tab_trip" to ("यात्रा खर्च" to "Trip Expense"),
    "fuel_amt_hint" to ("रकम (रु NRs)" to "Amount (NRs)"),
    "fuel_lit_hint" to ("इन्धन परिमाण (Liters)" to "Fuel Quantity (Liters)"),
    "fuel_dist_hint" to ("दूरी (कि.मि. km)" to "Distance (km)"),
    "fuel_mil_hint" to ("माइलेज (km/L)" to "Mileage (km/L)"),
    "fuel_res_lit" to ("प्राप्त हुने इन्धन परिमाण:" to "Fuel Quantity:"),
    "fuel_res_amt" to ("कुल लाग्ने रकम:" to "Total Fuel Cost:"),
    "fuel_res_req" to ("आवश्यक इन्धन:" to "Required Fuel:"),
    "fuel_res_est" to ("अनुमानित इन्धन खर्च:" to "Estimated Fuel Cost:"),
    "fuel_note" to ("नोट: ढुवानी दूरी अनुसार स्थान विशेष मूल्यमा थोरै फरक पर्न सक्छ।"
        to "Note: Prices vary slightly depending on transport distance and regional depot."),

    // radio
    "radio_title" to ("नेपाली एफएम रेडियो" to "Nepali FM Radio"),
    "radio_cat_all" to ("सबै" to "All"),
    "radio_cat_nat" to ("राष्ट्रिय" to "National"),
    "radio_cat_news" to ("समाचार" to "News"),
    "radio_cat_mus" to ("मनोरञ्जन" to "Music"),
    "radio_buffering" to ("बफरिङ हुँदैछ…" to "Buffering stream…"),
    "radio_playing" to ("लाइभ प्रसारण बजिरहेको छ" to "Playing Live Stream"),
    "radio_paused" to ("रोकिएको" to "Paused"),
    "radio_note" to ("अनलाइन लाइभ स्ट्रिम सुन्न इन्टरनेट जडान आवश्यक पर्दछ। रेडियो पृष्ठभूमिमा पनि बज्छ।"
        to "Live radio streaming requires an active internet connection. Plays in background."),

    // qr
    "qr_title" to ("क्युआर कोड जेनेरेटर" to "QR Generator"),
    "qr_type_web" to ("वेबसाइट" to "Website"),
    "qr_type_txt" to ("पाठ (Text)" to "Text"),
    "qr_type_ph" to ("फोन" to "Phone"),
    "qr_type_wifi" to ("वाइफाइ" to "Wi-Fi"),
    "qr_type_esewa" to ("eSewa" to "eSewa Pay"),
    "qr_hint_txt" to ("कुनै पनि पाठ वा सन्देश लेख्नुहोस्" to "Enter any text or message"),
    "qr_hint_web" to ("वेबसाइट URL (उदा. www.example.com)" to "Website URL (e.g. www.example.com)"),
    "qr_hint_ph" to ("फोन नम्बर (Phone Number)" to "Phone Number"),
    "qr_hint_wifi_s" to ("वाइफाइ नाम (Wi-Fi SSID)" to "Wi-Fi SSID (Network Name)"),
    "qr_hint_wifi_p" to ("वाइफाइ पासवर्ड" to "Wi-Fi Password"),
    "qr_hint_esewa" to ("eSewa ID / मोबाइल नम्बर" to "eSewa ID / Mobile Number"),
    "qr_scannable" to ("स्क्यान गर्न मिल्ने क्युआर कोड" to "Scannable QR Code"),
    "qr_copy_btn" to ("डेटा कपी गर्नुहोस्" to "Copy QR Data"),
    "qr_share_btn" to ("सेयर गर्नुहोस्" to "Share Data"),

    // common actions
    "close" to ("बन्द गर्नुहोस्" to "Close"),
    "copy" to ("कपी गर्नुहोस्" to "Copy"),
    "copied" to ("कपी गरियो" to "Copied"),
    "share" to ("सेयर गर्नुहोस्" to "Share"),
    "download" to ("डाउनलोड" to "Download"),
    "open" to ("खोल्नुहोस्" to "Open"),
    "save" to ("सेभ गर्नुहोस्" to "Save"),
    "remove" to ("हटाउनुहोस्" to "Remove"),
    "swap" to ("साट्नुहोस्" to "Swap"),
    "refresh" to ("रिफ्रेश" to "Refresh"),
    "search" to ("खोज्नुहोस्" to "Search"),
    "no_data" to ("डाटा छैन" to "No data"),
    "error" to ("त्रुटि" to "Error"),
    "success" to ("सफल भयो" to "Success"),

    // landrop
    "landrop_title" to ("द्रुत वाईफाई फाइल ट्रान्सफर" to "High-Speed LAN File Drop"),
    "landrop_share_host" to ("सेयर तथा सर्भर" to "Share & Host"),
    "landrop_received" to ("प्राप्त फाइलहरू" to "Received"),
    "landrop_no_files" to ("कुनै फाइल छैन" to "No files"),
    "landrop_save" to ("सेभ" to "Save"),
    "landrop_open" to ("खोल्नुहोस्" to "Open"),
    "landrop_share" to ("सेयर" to "Share"),
    "landrop_zip" to ("ZIP बनाउँदै..." to "Preparing ZIP..."),
    "landrop_download_all" to ("सबै ZIP मा डाउनलोड" to "Download All as ZIP"),

    // file converter
    "fc_save_failed" to ("सेभ गर्न असफल" to "Save failed"),
    "fc_zip_failed" to ("ZIP बनाउन असफल" to "ZIP failed"),
    "fc_pdf_error" to ("PDF बनाउन त्रुटि" to "Error creating PDF"),
    "fc_page" to ("पृष्ठ" to "Page"),

    // converter land
    "ropani" to ("रोपनी" to "Ropani"),
    "aana" to ("आना" to "Aana"),
    "paisa" to ("पैसा" to "Paisa"),
    "dam" to ("दाम" to "Dam"),
    "bigha" to ("बिघा" to "Bigha"),
    "kattha" to ("कट्ठा" to "Kattha"),
    "dhur" to ("धुर" to "Dhur"),
    "sqm" to ("वर्ग मिटर" to "Sq Meter"),
    "sqft" to ("वर्ग फिट" to "Sq Feet"),

    // bill
    "rupee" to ("रु" to "Rs."),
    "currency_rs" to ("रु" to "Rs."),

    // misc
    "back_desc" to ("पछाडि" to "Back"),
    "scan_qr" to ("QR स्क्यान गर्नुहोस्" to "Scan QR to Connect"),
    "unknown" to ("अज्ञात" to "Unknown"),

    // onboarding language only
    "onb_choose_lang" to ("भाषा छान्नुहोस्" to "Choose your language"),
    "onb_lang_hint" to ("नेपाली वा English छान्नुहोस्। सेटिङबाट पछि परिवर्तन गर्न सकिन्छ।" to "Select Nepali or English. You can change this anytime from Settings."),
    "onb_continue" to ("अघि बढ्नुहोस्" to "Continue"),
    "onb_change_later" to ("पछि सेटिङ > भाषा मा गएर परिवर्तन गर्न सकिन्छ" to "You can change language later in Settings > Language")
)

fun tt(key: String): String {
    val e = S[key] ?: return key
    return if (ThemePrefs.lang.value == "en") e.second else e.first
}

@Composable
fun T(key: String): String {
    val lang = ThemePrefs.lang.value
    val e = S[key] ?: return key
    return if (lang == "en") e.second else e.first
}
