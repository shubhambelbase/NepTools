package com.neptools.app.core.data.license

import android.content.Context
import com.neptools.app.R
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object DrivingLicenseQuestionBank {

    // In-memory cache for all 500+ parsed questions
    private var cachedQuestions: List<LicenseQuestion>? = null

    // =========================================================================
    // 1. TRAFFIC SIGNS & SYMBOLS DATABASE (Comprehensive DoTM Standard)
    // =========================================================================
    val trafficSigns = listOf(
        // ---------------------------------------------------------------------
        // MANDATORY SIGNS (आदेशमूलक तथा प्रतिबन्धात्मक - Official DoTM 28 Signs)
        // ---------------------------------------------------------------------
        TrafficSignItem("m_stop_give_way", "m_stop_give_way", SignCategory.MANDATORY, "रोक र जान देऊ", "Stop and Give Way", "सवारी पूर्ण रूपमा रोक्नुहोस् र अन्य सवारीलाई जान दिनुहोस्।", "Come to a complete stop and yield to oncoming vehicles.", "रु. १५०० जरिवाना।", "Fine of Rs. 1,500.", "circle", R.drawable.dotm_mand_01),
        TrafficSignItem("m_no_right_turn", "m_no_right_turn", SignCategory.MANDATORY, "दाहिने मोड्न निषेध", "No Right Turn", "दाहिने तर्फ मोड्न पूर्ण निषेध छ।", "Right turn is strictly prohibited.", "रु. ५०० - १००० जरिवाना।", "Fine of Rs. 500 - 1,000.", "circle", R.drawable.dotm_mand_02),
        TrafficSignItem("m_no_entry", "m_no_entry", SignCategory.MANDATORY, "प्रवेश निषेध", "No Entry", "यस सडक खण्डमा कुनै पनि सवारी प्रवेश गर्न निषेध छ।", "No vehicles allowed to enter this roadway.", "रु. ५०० देखि १५०० जरिवाना।", "Fine of Rs. 500 - 1,500.", "circle", R.drawable.dotm_mand_03),
        TrafficSignItem("m_weight_17t", "m_weight_17t", SignCategory.MANDATORY, "सवारी भार सीमा (१७ टन)", "Weight Limit (17 Tonnes)", "१७ टन भन्दा बढी तौल भएका सवारी प्रवेश निषेध।", "Vehicles exceeding 17 tonnes gross weight prohibited.", "रु. १५०० जरिवाना।", "Fine of Rs. 1,500.", "circle", R.drawable.dotm_mand_04),
        TrafficSignItem("m_speed_40", "m_speed_40", SignCategory.MANDATORY, "अधिकतम गति (४० किमी/घ)", "Maximum Speed Limit (40 km/h)", "अधिकतम ४० किमी प्रति घण्टाको गतिमा मात्र चलाउनुहोस्।", "Maximum permitted speed is 40 km/h.", "रु. १००० - १५०० जरिवाना।", "Fine of Rs. 1,000 - 1,500.", "circle", R.drawable.dotm_mand_05),
        TrafficSignItem("m_no_left_turn", "m_no_left_turn", SignCategory.MANDATORY, "बायाँ मोड्न निषेध", "No Left Turn", "बायाँ तर्फ मोड्न पूर्ण निषेध छ।", "Left turn is strictly prohibited.", "रु. ५०० - १००० जरिवाना।", "Fine of Rs. 500 - 1,000.", "circle", R.drawable.dotm_mand_06),
        TrafficSignItem("m_height_4_4m", "m_height_4_4m", SignCategory.MANDATORY, "सवारी उचाई सीमा (४.४ मिटर)", "Height Limit (4.4 Metres)", "४.४ मिटर भन्दा अग्लो सवारी साधन प्रवेश गर्न निषेध।", "Vehicles taller than 4.4m prohibited.", "रु. १००० जरिवाना।", "Fine of Rs. 1,000.", "circle", R.drawable.dotm_mand_07),
        TrafficSignItem("m_no_parking", "m_no_parking", SignCategory.MANDATORY, "पार्किङ्ग निषेध", "No Parking", "यस क्षेत्रमा सवारी साधन पार्किङ गर्न निषेध छ।", "Parking prohibited in this area.", "टोइङ + रु. १०००-२००० जरिवाना।", "Towing fee + Rs. 1,000 - 2,000.", "circle", R.drawable.dotm_mand_08),
        TrafficSignItem("m_stop_look_go", "m_stop_look_go", SignCategory.MANDATORY, "रोक, हेर र जाऊ", "Stop, Look and Go", "स्टप लाइनमा रोकिई दुवैतर्फ सुरक्षित भएपछि मात्र अघि बढ्नुहोस्।", "Stop at line, look both ways, and proceed only when clear.", "रु. १५०० जरिवाना।", "Fine of Rs. 1,500.", "circle", R.drawable.dotm_mand_09),
        TrafficSignItem("m_no_overtaking", "m_no_overtaking", SignCategory.MANDATORY, "उछिन्न निषेध", "Overtaking Prohibited", "अगाडिको कुनै पनि सवारीलाई ओभरटेक गर्न वा उछिन्न निषेध।", "Overtaking any moving vehicle is prohibited.", "रु. १५०० जरिवाना।", "Fine of Rs. 1,500.", "circle", R.drawable.dotm_mand_10),
        TrafficSignItem("m_axle_4t", "m_axle_4t", SignCategory.MANDATORY, "एक्सल भार सीमा (४ टन)", "Axle Weight Limit (4 Tonnes)", "प्रति एक्सल/धुरा ४ टन भन्दा बढी भार भएका सवारी निषेध।", "Axle load exceeding 4 tonnes prohibited.", "रु. १५०० जरिवाना।", "Fine of Rs. 1,500.", "circle", R.drawable.dotm_mand_11),
        TrafficSignItem("m_length_10m", "m_length_10m", SignCategory.MANDATORY, "सवारी लम्बाई सीमा (१० मिटर)", "Length Limit (10 Metres)", "१० मिटर भन्दा लामा सवारी साधन प्रवेश निषेध।", "Vehicles longer than 10 metres prohibited.", "रु. १००० जरिवाना।", "Fine of Rs. 1,000.", "circle", R.drawable.dotm_mand_12),
        TrafficSignItem("m_no_truck", "m_no_truck", SignCategory.MANDATORY, "ट्रक निषेध", "Trucks Prohibited", "भारी मालवाहक ट्रक तथा लरी प्रवेश निषेध।", "Heavy commercial trucks prohibited.", "रु. १५०० जरिवाना।", "Fine of Rs. 1,500.", "circle", R.drawable.dotm_mand_13),
        TrafficSignItem("m_no_u_turn", "m_no_u_turn", SignCategory.MANDATORY, "यु टर्न निषेध", "No U-Turn", "सवारी साधनलाई विपरित दिशामा १८० डिग्री घुमाउन निषेध।", "Turning vehicle 180 degrees is prohibited.", "रु. १००० जरिवाना।", "Fine of Rs. 1,000.", "circle", R.drawable.dotm_mand_14),
        TrafficSignItem("m_no_stopping", "m_no_stopping", SignCategory.MANDATORY, "रोक्न निषेध", "No Stopping / Clearway", "कुनै पनि अवस्थामा सवारी साधन रोक्न निषेध छ।", "Vehicles must not stop under any circumstances.", "रु. १००० जरिवाना।", "Fine of Rs. 1,000.", "circle", R.drawable.dotm_mand_15),
        TrafficSignItem("m_no_motor_vehicles", "m_no_motor_vehicles", SignCategory.MANDATORY, "मोटर सवारी निषेध", "All Motor Vehicles Prohibited", "दुईपाङ्ग्रे तथा चारपाङ्ग्रे सम्पूर्ण मोटर गाडी प्रवेश निषेध।", "All motor vehicles prohibited in both directions.", "रु. १००० जरिवाना।", "Fine of Rs. 1,000.", "circle", R.drawable.dotm_mand_16),
        TrafficSignItem("m_end_speed_40", "m_end_speed_40", SignCategory.MANDATORY, "गति सीमा समाप्त (४०)", "End of Speed Limit (40 km/h)", "४० किमीको गति सीमा प्रतिबन्ध समाप्त भएको संकेत।", "End of 40 km/h speed restriction.", "सडकको सामान्य गति नियम लागु।", "Standard speed rules apply.", "circle", R.drawable.dotm_mand_17),
        TrafficSignItem("m_ahead_turn_left", "m_ahead_turn_left", SignCategory.MANDATORY, "अगाडी गएर बायाँ मोड", "Turn Left Ahead", "अगाडि पुगेर अनिवार्य रूपमा बायाँ तर्फ मोड्नुहोस्।", "Compulsory turn left ahead (or right if opposite).", "रु. ५०० जरिवाना।", "Fine of Rs. 500.", "circle", R.drawable.dotm_mand_18),
        TrafficSignItem("m_ahead_only", "m_ahead_only", SignCategory.MANDATORY, "सिधा मात्र जाऊ", "Ahead Only", "दायाँ-बायाँ नमोडी सिधा अगाडि मात्र जानुहोस्।", "Mandatory to proceed straight ahead only.", "रु. ५०० - १००० जरिवाना।", "Fine of Rs. 500 - 1,000.", "circle", R.drawable.dotm_mand_19),
        TrafficSignItem("m_go_temp", "m_go_temp", SignCategory.MANDATORY, "जाऊ (अस्थायी चिन्ह)", "GO (Temporary Sign)", "अस्थायी ट्राफिक व्यवस्थापन अनुसार अगाडि बढ्नुहोस्।", "Temporary signal indicating traffic may proceed.", "निर्देशन पालना गर्नुहोस्।", "Follow officer instructions.", "circle", R.drawable.dotm_mand_20),
        TrafficSignItem("m_one_way_mand", "m_one_way_mand", SignCategory.MANDATORY, "एक तर्फी सवारी", "One Way Traffic", "तोकिएको दिशामा मात्र सवारी चलाउन पाइन्छ।", "Traffic moves only in the indicated direction.", "विपरित दिशामा रु. १५०० जरिवाना।", "Fine of Rs. 1,500 for wrong way.", "square", R.drawable.dotm_mand_21),
        TrafficSignItem("m_turn_left_only", "m_turn_left_only", SignCategory.MANDATORY, "बायाँ मोड", "Turn Left", "यस स्थानबाट अनिवार्य बायाँ तर्फ मोड्नुहोस्।", "Mandatory turn left (or right if opposite).", "रु. ५०० जरिवाना।", "Fine of Rs. 500.", "circle", R.drawable.dotm_mand_22),
        TrafficSignItem("m_end_restriction", "m_end_restriction", SignCategory.MANDATORY, "सीमा समाप्त", "End of All Restrictions", "अघिल्ला सबै ट्राफिक प्रतिबन्धहरू समाप्त भएको संकेत।", "All previous restrictions cease.", "सामान्य नियम लागु।", "Standard traffic rules apply.", "circle", R.drawable.dotm_mand_23),
        TrafficSignItem("m_stop_temp", "m_stop_temp", SignCategory.MANDATORY, "रोक (अस्थायी) चिन्ह", "STOP (Temporary Sign)", "अस्थायी निर्माण वा चेकपोस्टमा अनिवार्य रोकिनुहोस्।", "Mandatory temporary stop before the sign.", "रु. १५०० जरिवाना।", "Fine of Rs. 1,500.", "circle", R.drawable.dotm_mand_24),
        TrafficSignItem("m_pass_either_side", "m_pass_either_side", SignCategory.MANDATORY, "कुनैतिर बाट जाऊ", "Pass Either Side", "ट्राफिक आइल्याण्ड वा डिभाइडरको दायाँ वा बायाँ जुनसुकै तर्फबाट जान सकिन्छ।", "Vehicles may pass on either left or right side of barrier.", "रु. ५०० जरिवाना।", "Fine of Rs. 500.", "circle", R.drawable.dotm_mand_25),
        TrafficSignItem("m_give_way", "m_give_way", SignCategory.MANDATORY, "मुल सडक या गोल घुम्तीमा चल्नेलाई पहिले जान देउ", "Give Way / Yield", "मुख्य सडक वा गोलघुम्तीमा रहेका सवारीलाई पहिले जान दिनुहोस्।", "Yield right of way to vehicles on main road or roundabout.", "रु. ५०० - १००० जरिवाना।", "Fine of Rs. 500 - 1,000.", "triangle", R.drawable.dotm_mand_26),
        TrafficSignItem("m_roundabout_compulsory", "m_roundabout_compulsory", SignCategory.MANDATORY, "सानो गोल घुम्ती", "Mini Roundabout (Yield to Right)", "सानो गोल घुम्ती चोक: दाहिनेबाट आउने सवारीलाई जान दिनुहोस्।", "Roundabout navigation: Yield to traffic from the right.", "रु. ५०० जरिवाना।", "Fine of Rs. 500.", "circle", R.drawable.dotm_mand_27),
        TrafficSignItem("m_keep_left", "m_keep_left", SignCategory.MANDATORY, "बायाँ च्याप", "Keep Left", "सडक डिभाइडरको बायाँ तर्फ च्यापेर सवारी चलाउनुहोस्।", "Pass on the left side of divider (or right if opposite).", "रु. ५०० जरिवाना।", "Fine of Rs. 500.", "circle", R.drawable.dotm_mand_28),
        TrafficSignItem("m_no_horn", "m_no_horn", SignCategory.MANDATORY, "हर्न बजाउन निषेध", "No Honking / Silent Zone", "अस्पताल, विद्यालय तथा शान्त क्षेत्रमा हर्न बजाउन निषेध।", "No honking allowed in silent zones.", "रु. ५०० तत्काल जरिवाना।", "Instant fine of Rs. 500.", "circle"),
        TrafficSignItem("m_speed_20", "m_speed_20", SignCategory.MANDATORY, "गति सीमा २० किमी/घ", "Speed Limit (20 km/h)", "अधिकतम २० किमी प्रति घण्टाको गतिमा मात्र सवारी चलाउनुहोस्।", "Maximum permitted speed is 20 km/h.", "रु. १००० - १५०० जरिवाना।", "Fine of Rs. 1,000 - 1,500.", "circle"),
        TrafficSignItem("m_speed_30", "m_speed_30", SignCategory.MANDATORY, "गति सीमा ३० किमी/घ", "Speed Limit (30 km/h)", "अधिकतम ३० किमी प्रति घण्टाको गतिमा मात्र सवारी चलाउनुहोस्।", "Maximum permitted speed is 30 km/h.", "रु. १००० - १५०० जरिवाना।", "Fine of Rs. 1,000 - 1,500.", "circle"),
        TrafficSignItem("m_speed_50", "m_speed_50", SignCategory.MANDATORY, "गति सीमा ५० किमी/घ", "Speed Limit (50 km/h)", "अधिकतम ५० किमी प्रति घण्टाको गति सीमा।", "Maximum permitted speed is 50 km/h.", "रु. १००० - १५०० जरिवाना।", "Fine of Rs. 1,000 - 1,500.", "circle"),
        TrafficSignItem("m_speed_60", "m_speed_60", SignCategory.MANDATORY, "गति सीमा ६० किमी/घ", "Speed Limit (60 km/h)", "राजमार्गमा अधिकतम ६० किमी प्रति घण्टाको गति।", "Maximum permitted speed is 60 km/h.", "रु. १००० - १५०० जरिवाना।", "Fine of Rs. 1,000 - 1,500.", "circle"),
        TrafficSignItem("m_speed_80", "m_speed_80", SignCategory.MANDATORY, "गति सीमा ८० किमी/घ", "Speed Limit (80 km/h)", "द्रुतमार्गमा अधिकतम ८० किमी प्रति घण्टाको गति।", "Expressway maximum speed 80 km/h.", "रु. १५०० जरिवाना।", "Fine of Rs. 1,500.", "circle"),
        TrafficSignItem("m_sound_horn", "m_sound_horn", SignCategory.MANDATORY, "हर्न बजाउनु अनिवार्य", "Compulsory Sound Horn", "अन्धो घुम्ती वा पहाडी मोडमा हर्न बजाउन अनिवार्य छ।", "Mandatory to sound horn on blind bends.", "दुर्घटना जोखिम।", "Caution on hairpin turn.", "circle"),
        TrafficSignItem("m_no_bicycle", "m_no_bicycle", SignCategory.MANDATORY, "साइकल निषेध", "Cycles Prohibited", "यस सडक खण्डमा साइकल चलाउन पाइँदैन।", "Bicycles prohibited on this section.", "रु. २०० जरिवाना।", "Fine of Rs. 200.", "circle"),

        // ---------------------------------------------------------------------
        // CAUTIONARY / WARNING SIGNS (चेतावनीमूलक ट्राफिक चिन्हहरू - Official DoTM 40 Signs)
        // ---------------------------------------------------------------------
        TrafficSignItem("w_cross_road_minor", "w_cross_road_minor", SignCategory.CAUTIONARY, "चौबाटो (अगाडी शाखा सडक)", "Crossroad Ahead (Minor Branch Roads)", "अगाडि मूल सडकलाई साना शाखा सडकले काट्ने चारतर्फी चोक छ।", "Crossroads ahead with minor branch roads crossing.", "गति नियन्त्रण गर्नुहोस्।", "Reduce vehicle speed.", "triangle", R.drawable.dotm_warn_01),
        TrafficSignItem("w_cross_road_major", "w_cross_road_major", SignCategory.CAUTIONARY, "चौबाटो अगाडी (मूल सडक)", "Major Crossroad Ahead", "अगाडि मूल सडक भएको चारकुने चोक छ, सावधानीपूर्वक अघि बढ्नुहोस्।", "Major crossroad intersection ahead.", "दायाँ-बायाँ हेर्नुहोस्।", "Look both ways.", "triangle", R.drawable.dotm_warn_02),
        TrafficSignItem("w_side_road_right", "w_side_road_right", SignCategory.CAUTIONARY, "दाहिने शाखा सडक", "Side Road Right", "अगाडि दाहिने तर्फबाट शाखा सडक जोडिन्छ (अथवा बायाँ, यदि चिन्ह अर्को तर्फ भएमा)।", "Side road entering from the right.", "दायाँबाट आउने सवारीमा ध्यान दिनुहोस्।", "Watch for entering vehicles.", "triangle", R.drawable.dotm_warn_03),
        TrafficSignItem("w_pedestrian_crossing", "w_pedestrian_crossing", SignCategory.CAUTIONARY, "पदयात्रीले बाटो काट्ने ठाउँ", "Pedestrian Crossing Ahead", "अगाडि पैदलयात्रीले बाटो काट्ने जेब्रा क्रसिङ छ।", "Pedestrian crosswalk zone ahead. Prepare to stop.", "पैदलयात्रीलाई पहिलो प्राथमिकता।", "Yield to pedestrians.", "triangle", R.drawable.dotm_warn_04),
        TrafficSignItem("w_roundabout_ahead", "w_roundabout_ahead", SignCategory.CAUTIONARY, "गोल घुम्ती", "Roundabout Ahead", "अगाडि गोल घुम्ती चोक छ, दायाँबाट आउने सवारीलाई प्राथमिकता दिनुहोस्।", "Roundabout ahead. Yield to traffic on the right.", "लेन अनुशासन पालना गर्नुहोस्।", "Follow roundabout rules.", "triangle", R.drawable.dotm_warn_05),
        TrafficSignItem("w_sharp_right", "w_sharp_right", SignCategory.CAUTIONARY, "दाहिने मोड", "Right Curve Bend", "अगाडि दाहिने तर्फ घुम्ती छ (बायाँ, यदि चिन्ह अर्को तर्फ भएमा)।", "Right bend ahead (or left if sign is reversed).", "गति कम गर्नुहोस्।", "Slow down.", "triangle", R.drawable.dotm_warn_06),
        TrafficSignItem("w_hairpin_right", "w_hairpin_right", SignCategory.CAUTIONARY, "दाहिने पुरा मोड", "Hairpin Bend Right", "अगाडि दाहिने तर्फ तीव्र १८० डिग्रीको घुम्ती छ (बायाँ, यदि अर्को तर्फ भएमा)।", "Severe hairpin curve to the right.", "तल्लो गियर प्रयोग गर्नुहोस्।", "Use low gear.", "triangle", R.drawable.dotm_warn_07),
        TrafficSignItem("w_double_bend_left", "w_double_bend_left", SignCategory.CAUTIONARY, "दोहोरो मोड, पहिले बायाँ", "Double Bend, First Left", "अगाडि दोहोरो घुम्ती छ (पहिले बायाँ त्यसपछि दायाँ)।", "Double curve ahead starting to the left.", "ओभरटेक नगर्नुहोस्।", "No overtaking.", "triangle", R.drawable.dotm_warn_08),
        TrafficSignItem("w_narrow_road", "w_narrow_road", SignCategory.CAUTIONARY, "दुवैतिरबाट साँघुरिएको सडक", "Road Narrows Both Sides", "अगाडि सडक दुवैतर्फबाट साँघुरिँदै गएको छ।", "Road narrows ahead from both left and right.", "गति नियन्त्रण गर्नुहोस्।", "Control speed.", "triangle", R.drawable.dotm_warn_09),
        TrafficSignItem("w_narrow_road_right", "w_narrow_road_right", SignCategory.CAUTIONARY, "दाहिनेतिरबाट सांगुरिएको सडक", "Road Narrows on Right", "अगाडि सडक दाहिने तर्फबाट साँघुरिएको छ (बायाँ, यदि अर्को तर्फ भएमा)।", "Roadway narrows ahead from right side.", "लेन मिलाउनुहोस्।", "Merge safely.", "triangle", R.drawable.dotm_warn_10),
        TrafficSignItem("w_checkpost", "w_checkpost", SignCategory.CAUTIONARY, "जाँच चौकी", "Police Checkpost / Toll Barrier", "अगाडि प्रहरी जाँच चौकी वा ढाट छ, रोकिन तयार हुनुहोस्।", "Police security checkpost or toll barrier ahead.", "रोकिन तयार हुनुहोस्।", "Prepare to stop.", "triangle", R.drawable.dotm_warn_11),
        TrafficSignItem("w_narrow_bridge", "w_narrow_bridge", SignCategory.CAUTIONARY, "साँघुरो पुल", "Narrow Bridge Ahead", "अगाडि साँघुरो पुल छ, विपरित दिशाबाट आउने सवारीलाई ध्यान दिनुहोस्।", "Narrow bridge ahead. Yield to oncoming vehicles.", "पुलमा ओभरटेक निषेध।", "No passing on bridge.", "triangle", R.drawable.dotm_warn_12),
        TrafficSignItem("w_two_way_cross", "w_two_way_cross", SignCategory.CAUTIONARY, "अगाडी दुई तर्फी बाटो (दाहिने-बायाँ)", "Two-Way Traffic Crossing", "अगाडि क्रस हुने सडकमा दुई तर्फी सवारी साधन चल्छन्।", "Two-way traffic crossing ahead horizontally.", "दुवैतर्फ हेर्नुहोस्।", "Check both directions.", "triangle", R.drawable.dotm_warn_13),
        TrafficSignItem("w_two_way_straight", "w_two_way_straight", SignCategory.CAUTIONARY, "अगाडी दुई तर्फी बाटो (सिधा)", "Two-Way Traffic Straight", "एकतर्फी सडक समाप्त भई अगाडिबाट दुईतर्फी सवारी आवागमन सुरु हुन्छ।", "Two-way traffic ahead in the same road corridor.", "आफ्नो लेनमा रहनुहोस्।", "Stay in your lane.", "triangle", R.drawable.dotm_warn_14),
        TrafficSignItem("w_height_limit_warn", "w_height_limit_warn", SignCategory.CAUTIONARY, "अगाडी उचाई सीमा (४.४ मिटर)", "Height Limit Ahead (4.4m)", "अगाडि पुल वा सुरुङको उचाइ सीमा ४.४ मिटर छ।", "Height restriction overhead ahead (4.4m).", "अग्लो सवारी नछिराउनुहोस्।", "Avoid exceeding height limit.", "triangle", R.drawable.dotm_warn_15),
        TrafficSignItem("w_steep_descent", "w_steep_descent", SignCategory.CAUTIONARY, "बढी ओरालो", "Steep Descent Ahead", "अगाडि भिरालो ठाडो ओरालो छ, तल्लो गियर (Lower Gear) मा चलाउनुहोस्।", "Steep downhill gradient ahead. Use lower gear.", "इन्जिन ब्रेकिङ प्रयोग गर्नुहोस्।", "Use engine braking.", "triangle", R.drawable.dotm_warn_16),
        TrafficSignItem("w_dual_carriageway_end", "w_dual_carriageway_end", SignCategory.CAUTIONARY, "दोहोरो सडक समाप्त", "Dual Carriageway Ends", "डिभाइडर भएको दोहोरो सडक समाप्त भई एकल दुईतर्फी सडक सुरु हुन्छ।", "Dual carriageway road ends ahead.", "बायाँ लेनमा फर्कनुहोस्।", "Merge left safely.", "triangle", R.drawable.dotm_warn_17),
        TrafficSignItem("w_cattle_crossing", "w_cattle_crossing", SignCategory.CAUTIONARY, "पाल्तु जनावर", "Domestic Animals / Cattle Crossing", "सडकमा गाईवस्तु वा पाल्तु जनावर अचानक आउन सक्छन्।", "Cattle or livestock crossing road ahead.", "सवारी ढिलो गर्नुहोस्।", "Reduce speed.", "triangle", R.drawable.dotm_warn_18),
        TrafficSignItem("w_pedestrians_ahead", "w_pedestrians_ahead", SignCategory.CAUTIONARY, "अगाडि बाटोमा पदयात्रीहरू", "Pedestrians on Road Ahead", "सडकमा पैदल हिँड्ने मानिस वा बालबालिका हुन सक्छन्।", "Pedestrians or school children on road ahead.", "सतर्क रहनुहोस्।", "Drive with caution.", "triangle", R.drawable.dotm_warn_19),
        TrafficSignItem("w_low_flying_aircraft", "w_low_flying_aircraft", SignCategory.CAUTIONARY, "कम उचाईमा विमानहरू", "Low Flying Aircraft Ahead", "विमानस्थल नजिक भएकाले कम उचाइमा विमान उड्न सक्छन्, आवाजबाट नआत्तिनुहोस्।", "Low flying aircraft near airport zone.", "शान्त भई चलाउनुहोस्।", "Stay calm.", "triangle", R.drawable.dotm_warn_20),
        TrafficSignItem("w_t_junction", "w_t_junction", SignCategory.CAUTIONARY, "टी-जंक्शन", "T-Junction Ahead", "अगाडि टी-आकारको चोक छ जहाँबाट सिधा जान मिल्दैन।", "T-junction ahead. Road terminates.", "दायाँ वा बायाँ मोड्नुहोस्।", "Turn left or right.", "triangle", R.drawable.dotm_warn_21),
        TrafficSignItem("w_y_junction", "w_y_junction", SignCategory.CAUTIONARY, "वाई-जंक्शन", "Y-Junction Ahead", "अगाडि सडक दुई शाखामा विभाजित हुन्छ।", "Y-junction ahead. Road splits into two branches.", "सही लेन रोज्नुहोस्।", "Select proper lane.", "triangle", R.drawable.dotm_warn_22),
        TrafficSignItem("w_side_road_merge_right", "w_side_road_merge_right", SignCategory.CAUTIONARY, "दाहिनेबाट सवारी आउन सक्छ", "Side Road Merge Right", "अगाडि दाहिने तर्फको कोणबाट सवारी मुख्य सडकमा मिसिन सक्छन्।", "Side traffic merging at an angle from the right.", "मिसिने सवारीमा ध्यान दिनुहोस्।", "Watch for merging traffic.", "triangle", R.drawable.dotm_warn_23),
        TrafficSignItem("w_side_road_merge_left", "w_side_road_merge_left", SignCategory.CAUTIONARY, "बायाँबाट सवारी आउन सक्छ", "Side Road Merge Left", "अगाडि बायाँ तर्फको कोणबाट सवारी मुख्य सडकमा मिसिन सक्छन्।", "Side traffic merging at an angle from the left.", "मिसिने सवारीमा ध्यान दिनुहोस्।", "Watch for merging traffic.", "triangle", R.drawable.dotm_warn_24),
        TrafficSignItem("w_dip_causeway", "w_dip_causeway", SignCategory.CAUTIONARY, "खतरनाक दबेको बाटो", "Dip / Riverbed / Causeway", "अगाडि सडक सतह दबेको, खाल्डो वा खोलाको कजवे छ।", "Dip, riverbed, or low-water causeway on road ahead.", "सवारी बिस्तारै लैजानुहोस्।", "Cross slowly.", "triangle", R.drawable.dotm_warn_25),
        TrafficSignItem("w_traffic_signals", "w_traffic_signals", SignCategory.CAUTIONARY, "ट्राफिक संकेत", "Traffic Light Signals Ahead", "अगाडि ट्राफिक लाइट बत्ती प्रणाली छ, बत्तीको संकेत अनुसार चल्नुहोस्।", "Traffic control signal lights ahead. Prepare to stop.", "रातो बत्तीमा रोकिनुहोस्।", "Halt on red signal.", "triangle", R.drawable.dotm_warn_26),
        TrafficSignItem("w_speed_breaker", "w_speed_breaker", SignCategory.CAUTIONARY, "उठेको बाटो", "Speed Hump / Bump Ahead", "अगाडि सडकमा गति नियन्त्रक हम्प (Speed Breaker) छ।", "Speed hump on roadway ahead. Slow down.", "सवारी जोगाउनुहोस्।", "Protect vehicle suspension.", "triangle", R.drawable.dotm_warn_27),
        TrafficSignItem("w_unguarded_rail", "w_unguarded_rail", SignCategory.CAUTIONARY, "रेल गाडी (गेट नभएको)", "Unguarded Railway Crossing", "अगाडि गेट नभएको रेलवे क्रसिङ छ, रेल नआएको यकिन गरेर मात्र पार गर्नुहोस्।", "Unguarded railway level crossing without barrier ahead.", "रेललाई पहिलो प्राथमिकता।", "Trains have absolute right of way.", "triangle", R.drawable.dotm_warn_28),
        TrafficSignItem("w_steep_ascent", "w_steep_ascent", SignCategory.CAUTIONARY, "बढी उकालो", "Steep Ascent Ahead", "अगाडि ठाडो उकालो सडक छ, उचित शक्ति भएको गियरमा चलाउनुहोस्।", "Steep uphill gradient ahead. Maintain power gear.", "पछाडि गुल्टिन नदिनुहोस्।", "Prevent rollback.", "triangle", R.drawable.dotm_warn_29),
        TrafficSignItem("w_loose_gravel", "w_loose_gravel", SignCategory.CAUTIONARY, "गिट्टी उछिट्टिन सक्ने", "Loose Gravel / Flying Stones", "सडकमा गिट्टी छरिएकाले चक्काबाट ढुङ्गा उछिट्टिन सक्छ।", "Loose stones may be thrown by tires.", "अगाडिको सवारीसँग दूरी राख्नुहोस्।", "Increase following distance.", "triangle", R.drawable.dotm_warn_30),
        TrafficSignItem("w_river_bank", "w_river_bank", SignCategory.CAUTIONARY, "नदीको किनार", "River Bank / Quayside Ahead", "सडक सिधै नदी वा खोलाको किनार तर्फ पुग्छ।", "Road leads directly to river bank or quayside.", "सावधानीपूर्वक चलाउनुहोस्।", "Drive with caution near water.", "triangle", R.drawable.dotm_warn_31),
        TrafficSignItem("w_staggered_junction", "w_staggered_junction", SignCategory.CAUTIONARY, "एक पछि अर्को दोबाटोहरू", "Staggered Crossroads Ahead", "अगाडि थोरै दूरीको फरकमा बायाँ र दायाँ दुवैतर्फ शाखा सडकहरू छन्।", "Staggered side roads on left and right.", "गति घटाउनुहोस्।", "Reduce speed.", "triangle", R.drawable.dotm_warn_32),
        TrafficSignItem("w_slippery_road", "w_slippery_road", SignCategory.CAUTIONARY, "चिप्लो बाटो", "Slippery Road Ahead", "पानी वा हिलोका कारण सडक चिप्लो छ, अचानक ब्रेक नलगाउनुहोस्।", "Slippery road surface ahead. Avoid harsh braking.", "दुर्घटना जोखिम।", "High skidding risk.", "triangle", R.drawable.dotm_warn_33),
        TrafficSignItem("w_danger", "w_danger", SignCategory.CAUTIONARY, "खतरा", "General Danger / Hazard", "अगाडि अन्य कुनै खतरा वा जोखिम छ, सावधानीपूर्वक अघि बढ्नुहोस्।", "General danger hazard ahead. Proceed with high caution.", "सतर्क रहनुहोस्।", "Stay alert.", "triangle", R.drawable.dotm_warn_34),
        TrafficSignItem("w_hazard_marker_left", "w_hazard_marker_left", SignCategory.CAUTIONARY, "खतरनाक बाधा (बायाँ/दायाँ)", "Hazard Warning Marker", "सडक किनार वा पुलको मुखमा रहेको अवरोध चिन्ह।", "Hazard marker warning of roadside obstacle.", "अवरोध छल्नुहोस्।", "Clear the obstacle.", "square", R.drawable.dotm_warn_35),
        TrafficSignItem("w_diversion_arrow", "w_diversion_arrow", SignCategory.CAUTIONARY, "अस्थायी सडकको दिशा (डाइभर्सन)", "Temporary Diversion (Right)", "सडक मर्मतका कारण सवारीलाई अस्थायी बाटो तर्फ डाइभर्सन गरिएको संकेत।", "Temporary diversion route to the right.", "डाइभर्सन रुट पछ्याउनुहोस्।", "Follow diversion route.", "square", R.drawable.dotm_warn_37),
        TrafficSignItem("w_diversion_ahead", "w_diversion_ahead", SignCategory.CAUTIONARY, "अगाडी अस्थायी सडक (अगाडी डाइभर्सन)", "Diversion Ahead Sign", "अगाडि सडकमा डाइभर्सन आउँदैछ।", "Diversion ahead on roadway.", "गति कम गर्नुहोस्।", "Reduce speed.", "square", R.drawable.dotm_warn_38),
        TrafficSignItem("w_chevron_t_junction", "w_chevron_t_junction", SignCategory.CAUTIONARY, "टी-जंक्शन (दाहिने वा बायाँ मोड)", "T-Junction Chevron (Left or Right)", "टी-चोकमा दायाँ वा बायाँ मोड्न देखाइएको सेभ्रन बोर्ड।", "T-junction sharp turn chevron.", "मोड्न तयार हुनुहोस्।", "Prepare to turn.", "square", R.drawable.dotm_warn_39),
        TrafficSignItem("w_chevron_sharp_bend", "w_chevron_sharp_bend", SignCategory.CAUTIONARY, "तीखो मोड (अस्थायी बाटो)", "Sharp Bend Chevron Board", "तीव्र घुम्तीमा सुरक्षित दिशा देखाउने सेभ्रन बोर्ड।", "Sharp bend chevron marker on curve.", "गति नियन्त्रण गर्नुहोस्।", "Control vehicle speed.", "square", R.drawable.dotm_warn_40),

        // ---------------------------------------------------------------------
        // INFORMATORY SIGNS (जानकारीमूलक ट्राफिक चिन्हहरू - Official DoTM 19 Signs)
        // ---------------------------------------------------------------------
        TrafficSignItem("i_dead_end", "i_dead_end", SignCategory.INFORMATORY, "बाटोको अन्त्य", "Dead End / No Through Road", "अगाडि गएर सडक टुंगिन्छ (सिधा अगाडि जाने निकास छैन)।", "No through road / dead end ahead.", "यु-टर्न लिनुहोस्।", "Make U-turn.", "square", R.drawable.dotm_info_01),
        TrafficSignItem("i_pedestrian_cross_info", "i_pedestrian_cross_info", SignCategory.INFORMATORY, "पदयात्रीको बाटो", "Pedestrian Walkway Crossing", "पैदलयात्री सडक पार गर्ने स्थान।", "Designated pedestrian crossing walkway.", "पैदलयात्रीलाई बाटो दिनुहोस्।", "Yield to walkers.", "square", R.drawable.dotm_info_02),
        TrafficSignItem("i_parking_all", "i_parking_all", SignCategory.INFORMATORY, "पार्क गर्ने ठाउँ", "Parking Area", "सवारी साधन सुरक्षित रूपमा पार्क गर्ने आधिकारिक स्थान।", "Authorized public parking place.", "रेखा भित्र पार्क गर्नुहोस्।", "Park within marked bays.", "square", R.drawable.dotm_info_03),
        TrafficSignItem("i_passing_bay", "i_passing_bay", SignCategory.INFORMATORY, "उछिन्ने ठाउँ", "Passing / Overtaking Bay", "साँघुरो सडकमा अगाडिको गाडीलाई उछिन्न वा साइड दिन बनाइएको ठाउँ।", "Passing place or overtaking bay on narrow road.", "साइड दिनुहोस्।", "Use bay to pass.", "square", R.drawable.dotm_info_04),
        TrafficSignItem("i_telephone", "i_telephone", SignCategory.INFORMATORY, "टेलिफोन", "Public Telephone", "सार्वजनिक फोन सुविधा उपलब्ध छ।", "Public telephone booth available.", "—", "—", "square", R.drawable.dotm_info_05),
        TrafficSignItem("i_workshop", "i_workshop", SignCategory.INFORMATORY, "वर्क शप", "Vehicle Workshop / Repair", "सवारी मर्मत ग्यारेज तथा वर्कशप उपलब्ध छ।", "Vehicle repair workshop and mechanic facility.", "—", "—", "square", R.drawable.dotm_info_06),
        TrafficSignItem("i_petrol_pump", "i_petrol_pump", SignCategory.INFORMATORY, "पेट्रोल पम्प", "Petrol Pump / Fuel Station", "अगाडि पेट्रोल, डिजेल तथा इन्धन भर्ने स्टेशन उपलब्ध छ (२ किमी)।", "Fuel station ahead (2 km).", "धुम्रपान निषेध।", "No smoking near fuel.", "square", R.drawable.dotm_info_07),
        TrafficSignItem("i_resting_place", "i_resting_place", SignCategory.INFORMATORY, "बास बस्ने ठाउँ", "Resting Place / Motel / Lodge", "लामो यात्राका चालक तथा यात्रुका लागि विश्राम स्थल वा लज।", "Resting place, motel or accommodation.", "—", "—", "square", R.drawable.dotm_info_08),
        TrafficSignItem("i_restaurant", "i_restaurant", SignCategory.INFORMATORY, "रेष्टुराँ", "Restaurant / Eating Place", "खाना तथा भोजनालय सुविधा उपलब्ध छ।", "Restaurant and meal dining facility.", "—", "—", "square", R.drawable.dotm_info_09),
        TrafficSignItem("i_refreshment", "i_refreshment", SignCategory.INFORMATORY, "जलपान स्थल", "Refreshments / Snack Bar / Cafe", "खाजा, चिया तथा कफी सुविधा उपलब्ध छ।", "Snacks, tea, and cafeteria available.", "—", "—", "square", R.drawable.dotm_info_10),
        TrafficSignItem("i_hospital", "i_hospital", SignCategory.INFORMATORY, "अस्पताल", "Hospital Nearby (H)", "नजिकै अस्पताल छ, हर्न नबजाउनुहोस्।", "Hospital nearby. Silent zone enforced.", "शान्त क्षेत्र नियम लागु।", "Silent zone penalty.", "square", R.drawable.dotm_info_11),
        TrafficSignItem("i_cycle_track", "i_cycle_track", SignCategory.INFORMATORY, "साईकलको बाटो", "Cycle Track / Lane", "साइकल चालकका लागि मात्र तोकिएको समर्पित बाटो।", "Dedicated track exclusively for bicycles.", "मोटर गाडी प्रवेश निषेध।", "No motor vehicles.", "square", R.drawable.dotm_info_12),
        TrafficSignItem("i_picnic_spot", "i_picnic_spot", SignCategory.INFORMATORY, "वनभोज स्थल", "Picnic Spot", "वनभोज तथा मनोरञ्जन स्थल उपलब्ध छ।", "Authorized picnic and recreation site.", "फोहोर नगर्नुहोस्।", "Keep area clean.", "square", R.drawable.dotm_info_13),
        TrafficSignItem("i_pedestrian_way", "i_pedestrian_way", SignCategory.INFORMATORY, "पदयात्रीको बाटो", "Pedestrian Walkway Only", "पैदल हिँड्ने मानिसका लागि मात्र छुट्याइएको बाटो।", "Designated walkway exclusively for pedestrians.", "—", "—", "square", R.drawable.dotm_info_14),
        TrafficSignItem("i_pedestrian_cycle_track", "i_pedestrian_cycle_track", SignCategory.INFORMATORY, "पदयात्री र साईकलको बाटो", "Pedestrian & Cycle Track", "पैदलयात्री र साइकल दुवैका लागि तोकिएको सडक।", "Combined pathway for pedestrians and cyclists.", "—", "—", "square", R.drawable.dotm_info_15),
        TrafficSignItem("i_bus_stop", "i_bus_stop", SignCategory.INFORMATORY, "बस बिसौनी", "Bus Stop", "सार्वजनिक बस यात्रु चढाउन तथा ओराल्न रोकिने ठाउँ।", "Designated public bus passenger boarding stop.", "निजी सवारी रोक्न निषेध।", "No private car blockage.", "square", R.drawable.dotm_info_16),
        TrafficSignItem("i_one_way_info", "i_one_way_info", SignCategory.INFORMATORY, "एक तर्फी सडक", "One Way Road", "यो सडकमा तीरको दिशामा मात्र सवारी लैजान पाइन्छ।", "One way roadway in the arrow direction.", "उल्टो दिशामा निषेध।", "Strict penalty for wrong way.", "square", R.drawable.dotm_info_17),
        TrafficSignItem("i_taxi_park", "i_taxi_park", SignCategory.INFORMATORY, "ट्याक्सी पार्क", "Taxi Stand / Park", "भाडाका ट्याक्सी यात्रु पर्खने आधिकारिक स्थान।", "Authorized taxi rank and parking area.", "अन्य सवारी निषेध।", "No other vehicles.", "square", R.drawable.dotm_info_18),
        TrafficSignItem("i_place_name_duhabi", "i_place_name_duhabi", SignCategory.INFORMATORY, "ठाउँ चिनाउने चिन्ह (बस्तीमा प्रवेश)", "Place Name Sign (Duhabi)", "बस्ती वा नगर क्षेत्र सुरु भएको जानकारी दिने चिन्ह।", "Town / village boundary entry sign (e.g. Duhabi).", "गति घटाउनुहोस्।", "Slow down in town.", "square", R.drawable.dotm_info_19),

        // ---------------------------------------------------------------------
        // ROAD SURFACE MARKINGS (सडक रेखांकन - DoTM Standard)
        // ---------------------------------------------------------------------
        TrafficSignItem("r_broken_white", "r_broken_white", SignCategory.ROAD_MARKING, "खण्डित सेतो रेखा (Broken White Center Line)", "Broken White Center Line", "सडक सुरक्षित र स्पष्ट भएको अवस्थामा लेन परिवर्तन वा ओभरटेक गर्न पाइन्छ।", "Overtaking or lane changing permitted when clear and safe.", "इन्डिकेटर बालेर मात्र बदल्नुहोस्।", "Always signal before lane change.", "square"),
        TrafficSignItem("r_solid_white", "r_solid_white", SignCategory.ROAD_MARKING, "अखण्डित सेतो रेखा (Solid Continuous White Line)", "Solid White Center Line", "यो रेखा क्रस गरि ओभरटेक वा लेन परिवर्तन गर्न पूर्ण निषेध छ।", "Crossing this continuous line to overtake is strictly prohibited.", "रु. १००० लेन अनुशासन जरिवाना।", "Fine of Rs. 1,000.", "square"),
        TrafficSignItem("r_double_yellow", "r_double_yellow", SignCategory.ROAD_MARKING, "दोहोरो पहेँलो अखण्डित रेखा (Double Solid Yellow Lines)", "Double Solid Yellow Lines", "दुवै तर्फका कुनै पनि सवारीले रेखा क्रस गर्न वा ओभरटेक गर्न सख्त निषेध।", "Neither side of traffic may cross or straddle these lines.", "रु. १५०० जरिवाना।", "Fine of Rs. 1,500.", "square"),
        TrafficSignItem("r_solid_broken_combo", "r_solid_broken_combo", SignCategory.ROAD_MARKING, "खण्डित र अखण्डित संयुक्त रेखा (Solid with Broken Line)", "Combination Solid and Broken Line", "खण्डित रेखा भएको लेनका सवारीले ओभरटेक गर्न पाउँछन्, अखण्डित तर्फकाले पाउँदैनन्।", "Traffic on broken line side may pass; solid line side must not cross.", "रु. १००० जरिवाना।", "Fine of Rs. 1,000.", "square"),
        TrafficSignItem("r_zebra_crossing", "r_zebra_crossing", SignCategory.ROAD_MARKING, "जेब्रा क्रसिङ (Zebra Pedestrian Crossing)", "Zebra Crosswalk", "पैदलयात्री सडक पार गर्ने स्थान। सवारी रोकि पैदलयात्रीलाई पहिलो बाटो दिनु अनिवार्य।", "Pedestrian crosswalk zone. Drivers must stop and give priority.", "नरोकेमा रु. १५०० जरिवाना।", "Fine of Rs. 1,500.", "square"),
        TrafficSignItem("r_stop_line", "r_stop_line", SignCategory.ROAD_MARKING, "स्टप लाइन (Stop Line)", "Stop Line", "रातो बत्ती वा ट्राफिक प्रहरीको रोक्ने इशारा हुँदा यो रेखा अगावै रोकिनु पर्छ।", "Vehicles must come to a complete halt before this transverse line.", "रेखा नाघेमा रु. १५०० जरिवाना।", "Fine of Rs. 1,500.", "square"),
        TrafficSignItem("r_give_way_triangle", "r_give_way_triangle", SignCategory.ROAD_MARKING, "बाटो दिने त्रिकोण चिन्ह (Give Way Marking)", "Give Way Triangle Marking", "सडक सतहमा कोरिएको सेतो त्रिकोण: मुख्य सडकका सवारीलाई बाटो दिनुहोस्।", "Marked on road surface indicating requirement to yield.", "रु. ५०० जरिवाना।", "Fine of Rs. 500.", "square"),
        TrafficSignItem("r_yellow_box", "r_yellow_box", SignCategory.ROAD_MARKING, "पहेँलो बक्स जंक्शन (Yellow Box Junction)", "Yellow Box Junction", "चोकमा सवारी जाम हुँदा बक्स भित्र नपस्नुहोस्। अगाडिको निकास खुला भएपछि मात्र छिर्नुहोस्।", "Do not enter the box junction unless your exit is completely clear.", "रु. १००० जरिवाना।", "Fine of Rs. 1,000.", "square"),

        // ---------------------------------------------------------------------
        // TRAFFIC LIGHTS & POLICE SIGNALS (ट्राफिक लाइट तथा इशारा)
        // ---------------------------------------------------------------------
        TrafficSignItem("p_light_red", "p_light_red", SignCategory.TRAFFIC_LIGHT, "रातो बत्ती (Red Traffic Signal)", "Red Light Signal", "सवारी साधन स्टप लाइन अगावै पूर्ण रूपमा रोक्नुहोस्। क्रस गर्न सख्त निषेध।", "Stop completely before the stop line. No proceeding on red light.", "रु. १५०० जरिवाना।", "Fine of Rs. 1,500.", "light"),
        TrafficSignItem("p_light_yellow", "p_light_yellow", SignCategory.TRAFFIC_LIGHT, "पहेँलो बत्ती (Amber/Yellow Signal)", "Amber / Yellow Signal", "रातो बत्ती बल्न लागेको संकेत। सुरक्षित रूपमा रोकिने तयारी गर्नुहोस्।", "Prepare to halt safely before red. Do not speed up to beat the light.", "रु. ५०० - १००० जरिवाना।", "Fine of Rs. 500 - 1,000.", "light"),
        TrafficSignItem("p_light_green", "p_light_green", SignCategory.TRAFFIC_LIGHT, "हरियो बत्ती (Green Traffic Signal)", "Green Light Signal", "बाटो खुला छ, चोक खाली भएपछि सुरक्षित रूपमा अगाडि बढ्नुहोस्।", "Proceed through junction with care when safe.", "—", "—", "light"),
        TrafficSignItem("p_flashing_red", "p_flashing_red", SignCategory.TRAFFIC_LIGHT, "झिम्किने रातो बत्ती (Flashing Red Signal)", "Flashing Red Signal", "स्टप साइन सरह पूर्ण रूपमा रोक्नुहोस्, दुवैतर्फ हेरेर बाटो खाली भएपछि मात्र अघि बढ्नुहोस्।", "Treat as STOP sign: Complete halt, yield, and proceed only when clear.", "रु. १५०० जरिवाना।", "Fine of Rs. 1,500.", "light"),
        TrafficSignItem("p_flashing_yellow", "p_flashing_yellow", SignCategory.TRAFFIC_LIGHT, "झिम्किने पहेँलो बत्ती (Flashing Amber Signal)", "Flashing Amber Signal", "सवारीको गति कम गरी सावधानीपूर्वक चोक पार गर्नुहोस्।", "Slow down and proceed through intersection with extra caution.", "रु. ५०० - १००० जरिवाना।", "Fine of Rs. 500 - 1,000.", "light"),
        TrafficSignItem("p_green_arrow", "p_green_arrow", SignCategory.TRAFFIC_LIGHT, "हरियो बाण संकेत (Green Directional Arrow)", "Green Filter Arrow Signal", "मुख्य रातो बत्ती बले तापनि तीरले देखाएको दिशामा सुरक्षित मोड्न छुट छ।", "Permitted to turn in indicated arrow direction even if main light is red.", "—", "—", "light"),
        TrafficSignItem("p_pedestrian_red", "p_pedestrian_red", SignCategory.TRAFFIC_LIGHT, "पैदलयात्री रातो बत्ती (Don't Walk Red Man)", "Pedestrian Don't Walk (Red)", "पैदलयात्रीले सडक पार गर्न निषेध। फुटपाथमै पर्खनुहोस्।", "Pedestrians must not cross the roadway. Wait on sidewalk.", "सडक सुरक्षा उल्लंघन।", "Safety violation.", "light"),
        TrafficSignItem("p_pedestrian_green", "p_pedestrian_green", SignCategory.TRAFFIC_LIGHT, "पैदलयात्री हरियो बत्ती (Walk Green Man)", "Pedestrian Walk Signal (Green)", "पैदलयात्रीले जेब्रा क्रसिङबाट बाटो काट्न पाउँछन्। चालकले बाटो दिनुपर्छ।", "Pedestrians may cross at zebra crossing. Drivers must yield.", "पैदलयात्रीलाई बाटो दिनुहोस्।", "Yield to walkers.", "light"),
        TrafficSignItem("p_stop_front", "p_stop_front", SignCategory.TRAFFIC_LIGHT, "प्रहरीको अगाडिको सवारी रोक्ने इशारा", "Police Stop Front Traffic", "ट्राफिक प्रहरीले अगाडिबाट आउने सवारी साधनलाई रोकिन हात उठाएको आधिकारिक इशारा।", "Traffic police officer raising right hand to order oncoming traffic to stop.", "रु. १५०० जरिवाना।", "Fine of Rs. 1,500.", "square"),
        TrafficSignItem("p_stop_rear", "p_stop_rear", SignCategory.TRAFFIC_LIGHT, "प्रहरीको पछाडिको सवारी रोक्ने इशारा", "Police Stop Rear Traffic", "ट्राफिक प्रहरीले पछाडिबाट आउने सवारी साधनलाई रोकिन बायाँ हात तेर्स्याएको इशारा।", "Traffic police extending left arm horizontally to halt rear traffic.", "रु. १५०० जरिवाना।", "Fine of Rs. 1,500.", "square"),
        TrafficSignItem("p_stop_both", "p_stop_both", SignCategory.TRAFFIC_LIGHT, "प्रहरीको अगाडि र पछाडि दुवै रोक्ने इशारा", "Police Stop Front & Rear", "अगाडि र पछाडि दुवै तर्फबाट आउने सवारी साधनलाई एकैपटक रोक्ने संयुक्त इशारा।", "Simultaneous hand signal to halt both oncoming front and rear traffic.", "रु. १५०० जरिवाना।", "Fine of Rs. 1,500.", "square"),
        TrafficSignItem("p_beckon_left", "p_beckon_left", SignCategory.TRAFFIC_LIGHT, "प्रहरीको बायाँबाट सवारी पास गराउने इशारा", "Police Beckon Left Traffic", "ट्राफिक प्रहरीले बायाँ तर्फ रोकिएका सवारी साधनलाई अघि बढ्न दिएको इशारा।", "Traffic police beckoning vehicles waiting on the left side to move forward.", "इशारा पालना गर्नुहोस्।", "Follow officer direction.", "square"),
        TrafficSignItem("p_beckon_right", "p_beckon_right", SignCategory.TRAFFIC_LIGHT, "प्रहरीको दायाँबाट सवारी पास गराउने इशारा", "Police Beckon Right Traffic", "ट्राफिक प्रहरीले दायाँ तर्फका सवारी साधनलाई अघि बढ्न दिएको इशारा।", "Traffic police beckoning vehicles waiting on the right side to move forward.", "इशारा पालना गर्नुहोस्।", "Follow officer direction.", "square")
    )

    // 2. OFFICIAL 70-MARKS PRACTICAL TRIAL ASSESSMENT SCHEME (DoTM Standard)
    // =========================================================================
    val bikeTrialGuide = TrialGuide(
        category = LicenseCategory.CATEGORY_A,
        titleNp = "मोटरसाइकल / स्कुटर प्रयोगात्मक परीक्षा (Trial Guide - ७० अंक पास प्रणाली)",
        titleEn = "Motorcycle/Scooter Practical Trial Assessment (70 Marks Pass Rule)",
        totalMarks = 100,
        passMarks = 70,
        steps = listOf(
            TrialStep(
                stepNumber = 1,
                nameNp = "घुमाउरो ८ आकार (Figure 8 Test)",
                nameEn = "Figure '8' Maneuver",
                totalMarks = 25,
                deductionsNp = listOf(
                    "१ पटक सम्म खुट्टा टेकेमा: ५ अंक घट्ने (२ पटक टेकेमा फेल)",
                    "८ को पहेंलो रेखा छोएमा: ५ अंक घट्ने (१ पटक सम्म छुट)",
                    "८ आकार भन्दा बाहिर गएमा वा दिशा उल्टो भएमा: सिधै फेल"
                ),
                deductionsEn = listOf(
                    "1 foot touch: -5 marks deduction (2nd touch = Failed)",
                    "1 yellow boundary touch: -5 marks deduction",
                    "Driving out of track or wrong direction = Instant Fail"
                ),
                tipsNp = "क्लच र थ्रोटलको गति स्थिर राख्नुहोस्, अगाडि टाढा हेर्नुहोस् र ह्यान्डल सजिलै मोड्नुहोस्।",
                tipsEn = "Maintain steady clutch bite and low throttle. Look forward into the curve rather than down."
            ),
            TrialStep(
                stepNumber = 2,
                nameNp = "साँघुरो फल्याक / सीधा फल्याक (Narrow Plank Test)",
                nameEn = "Narrow Plank Balance",
                totalMarks = 15,
                deductionsNp = listOf(
                    "फल्याकबाट चक्का तल झरेमा: सिधै फेल",
                    "फल्याकमा खुट्टा टेकेमा: सिधै फेल"
                ),
                deductionsEn = listOf(
                    "Wheel falling off the plank = Instant Fail",
                    "Foot touching ground on plank = Instant Fail"
                ),
                tipsNp = "फल्याकमा प्रवेश गर्नुअगावै गति मिलाउनुहोस्, ह्यान्डल सिधा राखेर अगाडि सिधा हेर्नुहोस्।",
                tipsEn = "Enter with steady momentum. Keep eyes fixed at the far end of the plank."
            ),
            TrialStep(
                stepNumber = 3,
                nameNp = "यु-टर्न तथा साइड इन्डिकेटर (U-Turn & Indicator)",
                nameEn = "U-Turn with Indicator Signal",
                totalMarks = 10,
                deductionsNp = listOf(
                    "साइड इन्डिकेटर लाइट नबालेमा: १० अंक घट्ने",
                    "ट्राफिक रेखा छोएमा: ५ अंक घट्ने"
                ),
                deductionsEn = listOf(
                    "Failing to switch on side indicator: -10 marks",
                    "Touching traffic border line: -5 marks"
                ),
                tipsNp = "मोड्नु भन्दा पहिले दायाँ/बायाँ इन्डिकेटर अन गर्न नबिर्सनुहोस्।",
                tipsEn = "Always flick indicator light on before entering the turn."
            ),
            TrialStep(
                stepNumber = 4,
                nameNp = "ट्राफिक लाइट संकेत (Traffic Signal Stop & Go)",
                nameEn = "Traffic Light Compliance",
                totalMarks = 15,
                deductionsNp = listOf(
                    "रातो बत्ती बलेको बेला स्टप लाइन नाघेमा: सिधै फेल",
                    "पहेँलो/हरियो बत्तीको नियम उल्लंघन गरेमा: १५ अंक घट्ने"
                ),
                deductionsEn = listOf(
                    "Crossing stop line during Red light = Instant Fail",
                    "Improper signal compliance: -15 marks"
                ),
                tipsNp = "रातो बत्ती बल्दा स्टप लाइनभन्दा आधा मिटर अगावै रोकिनुहोस्। हरियो बलेपछि मात्र अघि बढ्नुहोस्।",
                tipsEn = "Stop completely before the white line on red. Proceed only on green."
            ),
            TrialStep(
                stepNumber = 5,
                nameNp = "गति अवरोधक / उबडखाबड (Speed Breaker / Bumps)",
                nameEn = "Speed Bump Maneuver",
                totalMarks = 15,
                deductionsNp = listOf(
                    "१ पटक खुट्टा टेकेमा: ५ अंक घट्ने",
                    "१ पटक इन्जिन बन्द (Start Off) भएमा: ५ अंक घट्ने"
                ),
                deductionsEn = listOf(
                    "1 foot touch: -5 marks deduction",
                    "1 engine stall: -5 marks deduction"
                ),
                tipsNp = "पहिलो गियरमा हल्का क्लच थिचेर बम्प्स सहजै पार गर्नुहोस्।",
                tipsEn = "Approach in 1st gear with gentle clutch slip over the bump."
            ),
            TrialStep(
                stepNumber = 6,
                nameNp = "उकालो र ओरालो र्याम्प (Uphill & Downhill Ramp)",
                nameEn = "Uphill & Downhill Ramp Test",
                totalMarks = 20,
                deductionsNp = listOf(
                    "तोकिएको रेखाभन्दा बाहिर रोकेमा: सिधै फेल",
                    "उकालोबाट गाडी अगाडि बढाउँदा ६ इन्चभन्दा बढी पछाडि सरेमा: सिधै फेल",
                    "१ पटक इन्जिन बन्द भएमा: ५ अंक घट्ने"
                ),
                deductionsEn = listOf(
                    "Stopping outside specified box lines = Instant Fail",
                    "Rolling back more than 6 inches = Instant Fail",
                    "Engine stall (1 time): -5 marks"
                ),
                tipsNp = "उकालोमा रोकेपछि फुट ब्रेक थिच्नुहोस्। अगाडि बढ्दा एक्सिलेटर दिएर क्लच छोड्दै जाँदा गाडी तान्न थालेपछि मात्र ब्रेक छोड्नुहोस्।",
                tipsEn = "Hold foot brake on ramp. Release clutch to bite point while applying gentle throttle before releasing brake."
            )
        )
    )

    val carTrialGuide = TrialGuide(
        category = LicenseCategory.CATEGORY_B,
        titleNp = "कार / जिप प्रयोगात्मक परीक्षा (Car Trial Guide - ७० अंक पास प्रणाली)",
        titleEn = "Car/Jeep Practical Trial Assessment (70 Marks Pass Rule)",
        totalMarks = 100,
        passMarks = 70,
        steps = listOf(
            TrialStep(
                stepNumber = 1,
                nameNp = "८ आकार घुम्ती (Figure 8 Test)",
                nameEn = "Car Figure 8 Maneuver",
                totalMarks = 20,
                deductionsNp = listOf(
                    "१ पटक सम्म रेखा छोएमा: १० अंक घट्ने (२ पटक छोएमा फेल)",
                    "८ आकारबाट बाहिर गएमा वा दिशा उल्टो भएमा: सिधै फेल"
                ),
                deductionsEn = listOf(
                    "1 line touch: -10 marks deduction (2nd touch = Fail)",
                    "Going out of track = Instant Fail"
                ),
                tipsNp = "गाडीलाई बाहिरी किनारा नजिक राखेर घुमाउँदा भित्री चक्का रेखामा लाग्दैन।",
                tipsEn = "Take wider line on the outside curve to prevent inside rear wheel from clipping."
            ),
            TrialStep(
                stepNumber = 2,
                nameNp = "ट्राफिक लाइट संकेत (Traffic Light Stop)",
                nameEn = "Traffic Light Compliance",
                totalMarks = 15,
                deductionsNp = listOf("रातो बत्तीमा स्टप लाइन काटेमा: सिधै फेल"),
                deductionsEn = listOf("Crossing stop line on red = Instant Fail"),
                tipsNp = "रातो बत्तीमा स्टप लाइन अगाडि पूरा ब्रेक लगाउनुहोस्।",
                tipsEn = "Full stop behind line."
            ),
            TrialStep(
                stepNumber = 3,
                nameNp = "उकालो र्याम्प स्टार्ट (Uphill Ramp Start)",
                nameEn = "Uphill Ramp Start with Handbrake",
                totalMarks = 20,
                deductionsNp = listOf(
                    "गाडी ६ इन्चभन्दा बढी पछाडि सरेमा: सिधै फेल",
                    "१ पटक इन्जिन अफ भएमा: ५ अंक घट्ने"
                ),
                deductionsEn = listOf(
                    "Rollback > 6 inches = Instant Fail",
                    "1 engine stall = -5 marks"
                ),
                tipsNp = "ह्यान्ड ब्रेक तानेर १ नम्बर गियरमा क्लच उठाउँदै इन्जिन भाइब्रेट भएपछि ह्यान्ड ब्रेक बिस्तारै छोड्नुहोस्।",
                tipsEn = "Pull handbrake securely. Find clutch biting point, rev lightly, and release handbrake smoothly."
            ),
            TrialStep(
                stepNumber = 4,
                nameNp = "एल-पार्किङ / टी-पार्किङ (L-Parking / Garage Parking)",
                nameEn = "Garage L-Parking & Reversing",
                totalMarks = 25,
                deductionsNp = listOf(
                    "१ पटक सम्म रेखा छोएमा: १० अंक घट्ने",
                    "पार्किङ खम्बा वा पोल ठोकेमा: सिधै फेल",
                    "१ पटक भन्दा बढी अगाडि-पछाडि मिलाएमा: ५ अंक घट्ने"
                ),
                deductionsEn = listOf(
                    "1 line touch: -10 marks",
                    "Hitting parking pole/curb = Instant Fail",
                    "More than 1 correction: -5 marks"
                ),
                tipsNp = "साइड मिरर र रियर भ्यू मिरर हेरेर बिस्तारै रिभर्स गर्नुहोस्।",
                tipsEn = "Use both side mirrors to gauge equal clearance on left and right poles."
            ),
            TrialStep(
                stepNumber = 5,
                nameNp = "सिट बेल्ट तथा साइड लाइट (Safety & Indicators)",
                nameEn = "Seatbelt & Indicators",
                totalMarks = 20,
                deductionsNp = listOf(
                    "सिट बेल्ट नबाँधेमा: १० अंक घट्ने",
                    "मोड्दा इन्डिकेटर नबालेमा: १० अंक घट्ने"
                ),
                deductionsEn = listOf(
                    "Not wearing seatbelt: -10 marks",
                    "Not using indicator: -10 marks"
                ),
                tipsNp = "गाडी स्टार्ट गर्नुअगावै सिट बेल्ट बाँध्नुहोस्।",
                tipsEn = "Fasten seatbelt before turning on the ignition."
            )
        )
    )

    /**
     * Loads the complete 500+ DoTM question dataset from assets (or cached memory).
     */
    fun getAllQuestions(context: Context? = null): List<LicenseQuestion> {
        cachedQuestions?.let { return it }

        if (context != null) {
            try {
                val inputStream = context.assets.open("dotm_questions.json")
                val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                val jsonText = reader.use { it.readText() }
                val jsonArr = JSONArray(jsonText)

                val list = mutableListOf<LicenseQuestion>()
                for (i in 0 until jsonArr.length()) {
                    val obj = jsonArr.getJSONObject(i)

                    val optNp = mutableListOf<String>()
                    val optNpArr = obj.getJSONArray("optionsNp")
                    for (j in 0 until optNpArr.length()) optNp.add(optNpArr.getString(j))

                    val optEn = mutableListOf<String>()
                    val optEnArr = obj.getJSONArray("optionsEn")
                    for (j in 0 until optEnArr.length()) optEn.add(optEnArr.getString(j))

                    val topicEnum = when (obj.getString("topic")) {
                        "operation" -> QuestionTopic.VEHICLE_OPERATION
                        "signs" -> QuestionTopic.TRAFFIC_SIGNS
                        "mechanical" -> QuestionTopic.MECHANICAL_KNOWLEDGE
                        "safety" -> QuestionTopic.ACCIDENT_POLLUTION
                        "law" -> QuestionTopic.LEGAL_FINES
                        else -> QuestionTopic.VEHICLE_OPERATION
                    }

                    val signSymbolStr = obj.optString("signSymbol").takeIf { it.isNotBlank() && it != "null" }

                    list.add(
                        LicenseQuestion(
                            id = obj.getInt("id"),
                            category = if (obj.optString("category") == "B") LicenseCategory.CATEGORY_B else LicenseCategory.CATEGORY_A,
                            topic = topicEnum,
                            questionNp = obj.getString("questionNp"),
                            questionEn = obj.getString("questionEn"),
                            optionsNp = optNp,
                            optionsEn = optEn,
                            correctIndex = obj.getInt("correctIndex"),
                            explanationNp = obj.getString("explanationNp"),
                            explanationEn = obj.getString("explanationEn"),
                            signSymbol = signSymbolStr
                        )
                    )
                }

                if (list.isNotEmpty()) {
                    cachedQuestions = list
                    return list
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return fallbackQuestions
    }

    /**
     * Generates a realistic 20-question DoTM Mock Exam set.
     * Balanced across all 5 official syllabus domains:
     * - Section A: Vehicle Operation (4 questions)
     * - Section B: Traffic Signs & Markings (4 questions with visual graphics)
     * - Section C: Mechanical Knowledge (4 questions)
     * - Section D: Safety & Pollution (4 questions)
     * - Section E: Laws & Fines (4 questions)
     */
    fun generateMockExam(context: Context?, category: LicenseCategory): List<LicenseQuestion> {
        val all = getAllQuestions(context)
        val filtered = all.filter { it.category == category || it.category == LicenseCategory.CATEGORY_A }

        val topicQuestions = mapOf(
            QuestionTopic.VEHICLE_OPERATION to filtered.filter { it.topic == QuestionTopic.VEHICLE_OPERATION }.shuffled().take(4),
            QuestionTopic.TRAFFIC_SIGNS to filtered.filter { it.topic == QuestionTopic.TRAFFIC_SIGNS }.shuffled().take(4),
            QuestionTopic.MECHANICAL_KNOWLEDGE to filtered.filter { it.topic == QuestionTopic.MECHANICAL_KNOWLEDGE }.shuffled().take(4),
            QuestionTopic.ACCIDENT_POLLUTION to filtered.filter { it.topic == QuestionTopic.ACCIDENT_POLLUTION }.shuffled().take(4),
            QuestionTopic.LEGAL_FINES to filtered.filter { it.topic == QuestionTopic.LEGAL_FINES }.shuffled().take(4)
        )

        val balanced = topicQuestions.values.flatten().toMutableList()

        if (balanced.size < 20) {
            val remaining = filtered.filterNot { balanced.contains(it) }.shuffled()
            balanced.addAll(remaining.take(20 - balanced.size))
        }

        return balanced.shuffled().take(20)
    }

    // Built-in fallback in case assets are still loading
    val fallbackQuestions = listOf(
        LicenseQuestion(
            id = 1,
            category = LicenseCategory.CATEGORY_A,
            topic = QuestionTopic.VEHICLE_OPERATION,
            questionNp = "सवारी साधन हाँक्दा कुन साइडबाट ओभरटेक (Overtake) गर्नुपर्छ?",
            questionEn = "From which side should you overtake a vehicle in Nepal?",
            optionsNp = listOf("दायाँ (Right) तर्फबाट", "बायाँ (Left) तर्फबाट", "जुनसुकै साइडबाट", "फुटपाथबाट"),
            optionsEn = listOf("From the Right side", "From the Left side", "From either side", "From footpath"),
            correctIndex = 0,
            explanationNp = "नेपालमा बायाँ तर्फ सवारी चलाउने नियम (Left-hand traffic) भएकाले सधैं अगाडिको सवारीको दायाँ तर्फबाट मात्र ओभरटेक गर्नुपर्छ।",
            explanationEn = "In Nepal's left-hand traffic system, overtaking must always be done from the right side."
        ),
        LicenseQuestion(
            id = 2,
            category = LicenseCategory.CATEGORY_A,
            topic = QuestionTopic.VEHICLE_OPERATION,
            questionNp = "सवारी मोड्नु भन्दा कम्तिमा कति मिटर अगाडि साइड इन्डिकेटर बाल्नुपर्छ?",
            questionEn = "How many meters before turning should you turn on the side indicator?",
            optionsNp = listOf("१० मिटर अगाडि", "३० मिटर अगाडि", "५ मिटर अगाडि", "मोड्ने समयमा मात्र"),
            optionsEn = listOf("10 meters ahead", "30 meters ahead", "5 meters ahead", "Only while turning"),
            correctIndex = 1,
            explanationNp = "पछाडिका तथा विपरित दिशाका चालकहरूलाई पर्याप्त समय दिन सवारी मोड्नुभन्दा कम्तिमा ३० मिटर अगाडि नै साइड लाइट बाल्नुपर्छ।",
            explanationEn = "Indicators should be on at least 30m prior to give adequate reaction time to others."
        ),
        LicenseQuestion(
            id = 3,
            category = LicenseCategory.CATEGORY_A,
            topic = QuestionTopic.VEHICLE_OPERATION,
            questionNp = "आपतकालीन अवस्थामा (जस्तै: एम्बुलेन्स वा दमकल आउँदा) के गर्नुपर्छ?",
            questionEn = "What should you do when an emergency vehicle (Ambulance/Fire Brigade) approaches?",
            optionsNp = listOf(
                "आफ्नो सवारी बायाँ किनारमा लगेर रोकी बाटो दिनुपर्छ",
                "सवारीको गति बढाएर अगाडि दगुराउनुपर्छ",
                "बीच सडकमै ब्रेक लगाएर रोक्नुपर्छ",
                "हर्न बजाउँदै अघि बढ्नुपर्छ"
            ),
            optionsEn = listOf(
                "Pull over to the left side and stop to give way",
                "Accelerate and drive ahead fast",
                "Stop in the middle of the road",
                "Honk and keep driving"
            ),
            correctIndex = 0,
            explanationNp = "एम्बुलेन्स, दमकल, तथा शव वाहनलाई पहिलो प्राथमिकता (Right of Way) दिँदै बायाँ किनारामा सवारी सारेर रोक्नुपर्छ।",
            explanationEn = "Emergency vehicles have top priority. Always pull safely to the left edge and stop."
        )
    )
}
