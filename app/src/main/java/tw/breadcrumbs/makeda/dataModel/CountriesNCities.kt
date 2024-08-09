package tw.breadcrumbs.makeda.dataModel


class CountriesNCities {
    val country_str = arrayOf("台灣", "日本", "中國")

    val TW_areas_str = arrayOf("彰化縣", "南投縣","雲林縣",
        "嘉義縣", "台南市", "高雄市", "屏東縣", "台東縣",
        "花蓮縣", "宜蘭縣", "基隆市", "新北市", "台北市",
        "桃園市", "新竹縣", "苗栗縣", "台中市",
        "澎湖縣", "金門縣", "連江縣")

    val JPAreasENstr = arrayOf("Hokkaido", "Aomori", "Iwate", "Miyagi", "Akita", "Yamagata", "Fukushima",
        "Ibaraki", "Tochigi", "Gunma", "Saitama", "Chiba", "Tokyo", "Kanagawa",
        "Niigata", "Toyama", "Ishikawa", "Fukui", "Yamanashi", "Nagano", "Gifu","Shizuoka", "Aichi",
        "Mie", "Shiga", "Kyoto", "Osaka", "Hyogo", "Nara", "Wakayama",
        "Tottori", "Shimane", "Okayama", "Hiroshima", "Yamaguchi",
        "Tokushima", "Kagawa", "Ehime", "Kochi",
        "Fukuoka", "Saga", "Nagasaki", "Kumamoto", "Oita", "Miyazaki", "Kagoshima",
        "Okinawa")


    val JP_areas_ZH_str = arrayOf("北海道", "青森縣", "岩手縣", "宮城縣", "秋田縣", "山形縣","福島縣",
        "茨城縣", "栃木縣", "群馬縣", "埼玉縣", "千葉縣", "東京都", "神奈川縣",
        "新潟縣", "富山縣", "石川縣", "福井縣", "山梨縣", "長野縣", "岐阜縣", "靜岡縣", "愛知縣",
        "三重縣", "滋賀縣", "京都府", "大阪府", "兵庫縣", "奈良縣", "和歌山縣",
        "鳥取縣", "島根縣", "岡山縣", "廣島縣", "山口縣",
        "德島縣", "香川縣", "愛媛縣", "高知縣",
        "福岡縣", "佐賀縣", "長崎縣", "熊本縣", "大分縣", "宮崎縣", "鹿兒島縣",
        "沖繩縣")

    val CN_areas_str = arrayOf("北京市", "天津市", "上海市", "重慶市",
        "河北省", "山西省", "遼寧省", "吉林省", "黑龍江省",
        "江蘇省", "浙江省", "安徽省", "福建省", "江西省", "山東省", "河南省",
        "湖北省", "湖南省", "廣東省", "海南省", "四川省", "貴州省", "雲南省",
        "陝西省", "甘肅省", "青海省",
        "內蒙古", "廣西", "西藏", "寧夏", "新疆",
        "香港", "澳門")


    fun isCJK(str: String):Boolean {

        for (ch in str.toCharArray()) {
            val block = Character.UnicodeBlock.of(ch)
            if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS == block ||
                Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS == block ||
                Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A == block
            ) {
                return true
            }
        }
        return false
    }

}