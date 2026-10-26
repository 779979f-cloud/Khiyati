package com.khiyati.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.view.Gravity
import android.widget.*
import org.json.JSONObject
import org.json.JSONArray
import kotlin.math.max

private data class Garment(
    val id: String, val name: String, val category: String, val level: String,
    val summary: String, val pieces: List<String>, val steps: List<String>,
    val comfort: Double, val seam: Double
)

class MainActivity : Activity() {
    private val plum = Color.rgb(105, 55, 88)
    private val dark = Color.rgb(54, 42, 50)
    private val cream = Color.rgb(255, 248, 242)
    private val rose = Color.rgb(242, 226, 235)
    private lateinit var content: LinearLayout
    private var currentScreen = "home"
    private var selectedCategory = "الكل"
    private var selectedGarment: Garment? = null
    private val measurements = linkedMapOf(
        "محيط الصدر" to 92.0, "محيط الخصر" to 76.0, "محيط الأرداف" to 100.0,
        "عرض الكتفين" to 38.0, "طول الظهر" to 40.0, "طول القطعة" to 100.0,
        "طول الكم" to 58.0, "محيط أعلى الذراع" to 30.0, "محيط الرقبة" to 36.0
    )
    private val measurementInputs = linkedMapOf<String, EditText>()
    private val models = listOf(
        Garment("dress-a", "فستان واسع يومي", "فساتين", "مبتدئ",
            "تصميم واسع مناسب للتدريب الأول. الحسابات أدناه تقديرية وتحتاج باترونًا تجريبيًا.",
            listOf("أمام على خط الطي", "خلف على خط الطي", "كمّان اختياريان", "شريط رقبة أو بييه"),
            listOf("اغسل القماش وجففه واكوه وفق تعليمات الخامة.",
                "خذ محيط الصدر والخصر والأرداف وطول الفستان وعرض الكتفين.",
                "ارسم الأمام والخلف مع تحديد خط الطي وخط النسيج، ثم أضف بدل الخياطة.",
                "قص قطعة تجريبية من قماش رخيص وثبّت الجانبين مؤقتًا.",
                "جرّب الاتساع وحرية الحركة، وعدّل الباترون قبل قص القماش النهائي.",
                "خيّط الكتفين ثم الجانبين، وثبّت الأكمام إن وجدت، ثم أنهِ الرقبة والحاشية."),
            12.0, 1.0),
        Garment("dress-b", "فستان محدد الخصر", "فساتين", "متوسط",
            "يحتاج ضبط بنسات أو قصّات بحسب شكل الجسم. لا تستخدم نسبًا عامة دون بروفة.",
            listOf("صدر أمامي", "صدر خلفي", "تنورة أمامية", "تنورة خلفية", "سحاب", "بطانة اختيارية"),
            listOf("سجّل الصدر والخصر والأرداف وطول الصدر والظهر.",
                "حدّد موضع الخصر وطول التنورة من التصميم المطلوب.",
                "ارسم بنسات الصدر والخصر أو قصّات الأميرة على الباترون.",
                "أضف بدل الخياطة بحسب نوع الوصلة وتعليمات الباترون.",
                "سرّج القطع وجربها؛ عدّل البنسات قبل تركيب السحاب.",
                "ركّب الصدر والتنورة، ثم السحاب، ثم البطانة والحاشية."),
            6.0, 1.0),
        Garment("dress-c", "فستان سهرة ببطانة", "فساتين", "متقدم",
            "افحص تمدد القماش وشفافيته واتجاه اللمعة. قد تتطلب الخامة تدعيمًا خاصًا.",
            listOf("قطع خارجية", "قطع البطانة", "تدعيم حسب الخامة", "سحاب مخفي"),
            listOf("اختبر القماش والبطانة على قصاصة صغيرة.",
                "خذ المقاسات مع تحديد مستوى الراحة المناسب للتصميم.",
                "انقل الباترون إلى القماش الخارجي والبطانة مع تمييز كل قطعة.",
                "ثبّت مناطق الشد حسب نوع القماش وتعليمات التدعيم.",
                "نفّذ بروفة كاملة قبل تركيب السحاب النهائي.",
                "ركّب البطانة ونظّف الحواف واكْوِ بدرجة مناسبة للخامة."),
            4.0, 1.0),
        Garment("coat-a", "كوت مستقيم", "أكوات ومعاطف", "متوسط",
            "اترك راحة إضافية للملابس التي ستُلبس تحته. راعِ سماكة القماش والبطانة.",
            listOf("أمام أيمن", "أمام أيسر", "ظهر", "كمّان", "ياقة", "بطانة", "جيب اختياري"),
            listOf("قِس الصدر والكتفين وطول المعطف والكم ومحيط أعلى الذراع.",
                "أضف راحة مناسبة للملابس أسفل المعطف؛ الراحة تختلف باختلاف الخامة.",
                "افحص اتجاه الوبر أو النقشة ورتّب القطع مع تطابقها.",
                "قص القماش الخارجي والبطانة والتدعيم كلًا حسب تعليمات الباترون.",
                "ركّب الجيوب والكتفين والياقة ثم الأكمام والجوانب.",
                "ركّب البطانة والأزرار، وافحص حرية الحركة قبل التشطيب."),
            14.0, 1.2),
        Garment("coat-b", "كوت واسع بأزرار", "أكوات ومعاطف", "مبتدئ",
            "تصميم واسع نسبيًا؛ يظل طول الياقة ومرد الأزرار تابعًا للرسم المختار.",
            listOf("أمامان مع مرد", "ظهر على خط الطي", "كمّان", "ياقة", "جيبان"),
            listOf("حدّد طول الكوت وموضع الأزرار والجيوب.",
                "قِس الصدر والكتفين والكم وأضف راحة للطبقات الداخلية.",
                "ارسم خط المنتصف ومرد الأزرار والياقة وفق باترون محدد.",
                "قص بعد التأكد من اتجاه القماش وتطابق القطع.",
                "ركّب الجيوب ثم الكتفين والياقة والأكمام.",
                "ثبّت الأزرار وجرّب الكوت فوق الملابس المعتادة."),
            16.0, 1.2),
        Garment("robe-a", "جرامة/جلابية مستقيمة", "جرامات وأرواب", "مبتدئ",
            "اسم القطعة قد يختلف محليًا. تأكد من الشكل المطلوب قبل رسم الباترون.",
            listOf("أمام", "ظهر", "كمّان", "بييه أو مرد رقبة", "جيوب اختيارية"),
            listOf("قِس الصدر والأرداف والطول وعرض الكم وفتحة الرقبة.",
                "حدّد الاتساع المطلوب وطول الشقوق الجانبية إن وجدت.",
                "اطوِ القماش فقط إذا كان اتجاه النسيج والتماثل يسمحان بذلك.",
                "ارسم الأمام والخلف مع تحديد خط الطي، ثم أضف بدل الخياطة.",
                "سرّج الكتفين والجوانب وجرب الحركة والجلوس.",
                "ركّب الأكمام والرقبة والجيوب، ثم أنهِ الحاشية."),
            18.0, 1.0),
        Garment("robe-b", "روب نسائي بحزام", "جرامات وأرواب", "مبتدئ",
            "يلزم تحديد طول الروب وعرض اللفّة ومكان الحزام قبل القص.",
            listOf("أمامان متداخلان", "ظهر", "كمّان", "حزام", "حلقات حزام"),
            listOf("قِس الطول والصدر والأرداف والكم.",
                "حدّد مقدار التداخل الأمامي وعرض الحزام.",
                "ارسم الظهر على الطي والأمامَين مع بدل الخياطة.",
                "قص الحزام والحلقات وفق الطول المطلوب مع اختبار عقدة الحزام.",
                "ركّب الكتفين والجوانب ثم الأكمام.",
                "أنه الحواف وثبّت حلقات الحزام وجرب الروب."),
            20.0, 1.0),
        Garment("abaya-a", "عباءة مستقيمة", "عباءات وملابس يمنية", "مبتدئ",
            "تصميم عام؛ تختلف العباءات اليمنية حسب المنطقة والقَصّة والتطريز.",
            listOf("أمامان", "ظهر على خط الطي", "كمّان واسعان", "حافة أمامية"),
            listOf("حدّد إن كانت العباءة مفتوحة أو مغلقة، ومقدار الاتساع المطلوب.",
                "قِس طول العباءة والصدر والأرداف والكتف والكم.",
                "اختر عرض الكم وفتحة اليد حسب التصميم.",
                "رتّب الباترون مع مراعاة اتجاه النسيج وبدل الحواف.",
                "ركّب الكتفين والجوانب والأكمام، وجربها قبل التشطيب.",
                "أنه الحافة الأمامية والكمّين والحاشية، ثم أضف الزخرفة إن رغبت."),
            22.0, 1.0),
        Garment("yemen-a", "ثوب/لباس يمني واسع", "عباءات وملابس يمنية", "متوسط",
            "هذه بطاقة تدريب عامة وليست توثيقًا لزي منطقة بعينها؛ اختر صورة مرجعية دقيقة قبل القص.",
            listOf("أمام", "ظهر", "قطع جانبية حسب الموديل", "أكمام", "فتحة رقبة"),
            listOf("اختر صورة مرجعية وحدد شكل الرقبة والأكمام والاتساع.",
                "خذ قياسات الجسم والطول المطلوب وسجّلها.",
                "ارسم كل قطعة مستقلة ودوّن عدد مرات القص وخط النسيج.",
                "أضف بدل الخياطة المناسب لكل وصلة.",
                "جرّب القطع مؤقتًا وتأكد من حركة الذراع وطول الثوب.",
                "ركّب الوصلات حسب الباترون ثم أنهِ الرقبة والأكمام والحاشية."),
            20.0, 1.0)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadMeasurements()
        showHome()
    }

    private fun shell(title: String, subtitle: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setBackgroundColor(cream)
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 22, 20, 18)
            setBackgroundColor(plum)
        }
        header.addView(TextView(this).apply {
            text = title; textSize = 25f; setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        })
        header.addView(TextView(this).apply {
            text = subtitle; textSize = 13f; setTextColor(Color.rgb(255, 232, 243))
            setPadding(0, 5, 0, 0)
        })
        root.addView(header)
        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 12, 14, 18)
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply { setBackgroundColor(Color.WHITE); setPadding(2, 4, 2, 4) }
        listOf("الرئيسية", "الموديلات", "المقاسات", "دليل القص").forEachIndexed { i, name ->
            nav.addView(Button(this).apply {
                text = name; textSize = 10.5f; setTextColor(plum)
                setOnClickListener { when(i) {
                    0 -> showHome()
                    1 -> showModels()
                    2 -> showMeasurements()
                    3 -> showGuide()
                } }
            }, LinearLayout.LayoutParams(0, -2, 1f))
        }
        root.addView(nav)
        setContentView(root)
    }

    private fun text(s: String, size: Float = 15f, bold: Boolean = false) {
        content.addView(TextView(this).apply {
            text = s; textSize = size; setTextColor(dark)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setPadding(3, 7, 3, 7)
        })
    }

    private fun button(s: String, action: () -> Unit) {
        content.addView(Button(this).apply {
            text = s; setTextColor(Color.WHITE); setBackgroundColor(plum)
            setOnClickListener { action() }
        })
    }

    private fun card(title: String, description: String, action: () -> Unit) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 8, 12, 8)
            setBackgroundColor(Color.WHITE)
        }
        box.addView(TextView(this).apply {
            text = title; textSize = 18f; setTextColor(plum); typeface = Typeface.DEFAULT_BOLD
        })
        box.addView(TextView(this).apply {
            text = description; textSize = 13.5f; setTextColor(dark); setPadding(0, 5, 0, 5)
        })
        box.addView(Button(this).apply {
            text = "افتح الدليل"; setTextColor(Color.WHITE); setBackgroundColor(plum)
            setOnClickListener { action() }
        })
        val lp = LinearLayout.LayoutParams(-1, -2); lp.setMargins(0, 5, 0, 9)
        content.addView(box, lp)
    }

    private fun showHome() {
        currentScreen = "home"
        shell("خياطتي ✂", "تعلم القص والخياطة خطوة بخطوة — يعمل دون إنترنت")
        text("أهلًا بك في مكتبة الخياطة", 21f, true)
        text("اختر موديلًا، أدخل مقاساتك، ثم راجع حسابات الباترون وخطوات القص والتركيب. الصور التوضيحية هنا رسومات محلية تعمل دون إنترنت.")
        button("👗 مكتبة الموديلات") { showModels() }
        button("📏 المقاسات وحساب الأبعاد") { showMeasurements() }
        button("🧵 دليل القص والتركيب") { showGuide() }
        text("تنبيه الخياطة", 18f, true)
        text("الحسابات المبدئية لا تستبدل الباترون الاحترافي. تختلف الراحة وبدل الخياطة حسب القماش والتصميم والجسم. نفّذ بروفة على قماش تجريبي قبل قص القماش النهائي.")
        text("الأقسام", 18f, true)
        listOf("فساتين", "أكوات ومعاطف", "جرامات وأرواب", "عباءات وملابس يمنية").forEach {
            button(it) { selectedCategory = it; showModels() }
        }
    }

    private fun showModels() {
        currentScreen = "models"
        shell("مكتبة الموديلات", "اختر القسم ثم افتح بطاقة الموديل")
        val cats = listOf("الكل", "فساتين", "أكوات ومعاطف", "جرامات وأرواب", "عباءات وملابس يمنية")
        cats.forEach { cat ->
            button(if (selectedCategory == cat) "✓ $cat" else cat) {
                selectedCategory = cat; showModels()
            }
        }
        val visible = models.filter { selectedCategory == "الكل" || it.category == selectedCategory }
        visible.forEach { m ->
            card(m.name, "${m.category} • ${m.level}\n${m.summary}") { showGarment(m) }
        }
    }

    private fun showGarment(m: Garment) {
        selectedGarment = m
        currentScreen = "detail"
        shell(m.name, "${m.category} • ${m.level}")
        text("رسم تخطيطي توضيحي", 18f, true)
        content.addView(PatternSketch(this, m.id), LinearLayout.LayoutParams(-1, 300))
        text("القطع المطلوبة", 18f, true)
        m.pieces.forEachIndexed { i, piece -> text("${i+1}. $piece") }
        text("خطوات القص والتركيب", 18f, true)
        m.steps.forEach { text(it) }
        text("حساب أولي لعرض ربع الجسم", 18f, true)
        text("الصيغة: (محيط الصدر + راحة التصميم) ÷ 4 + بدل خياطة جانبي واحد. الراحة في هذا الموديل: ${m.comfort} سم للمحيط الكامل. بدل الخياطة الافتراضي: ${m.seam} سم لكل حافة.")
        val chest = measurements["محيط الصدر"] ?: 0.0
        val quarter = (chest + m.comfort) / 4.0 + m.seam
        text("باستخدام محيط الصدر المحفوظ (${fmt(chest)} سم): عرض ربع تقريبي = ${fmt(quarter)} سم.")
        text("هذا الحساب لا يرسم باترونًا كاملًا ولا يحسب البنسات أو ميل الكتف أو دوران الكم. لا تعتمد عليه وحده للقص النهائي.")
        button("تعديل المقاسات والحساب") { showMeasurements() }
        button("رجوع إلى المكتبة") { showModels() }
    }

    private fun showMeasurements() {
        currentScreen = "measure"
        shell("المقاسات والحساب", "السنتيمتر • أدخل قياساتك الحقيقية")
        text("طريقة القياس: استخدم شريط قياس أفقيًا دون شده، واطلب مساعدة شخص عند قياس الظهر والكتفين. القيم المعروضة حاليًا أمثلة قابلة للتعديل.")
        measurementInputs.clear()
        measurements.forEach { (name, value) ->
            text(name, 14f, true)
            val input = EditText(this).apply {
                setSingleLine(true); textSize = 16f
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                setText(fmt(value)); setPadding(10, 3, 10, 3); setBackgroundColor(Color.WHITE)
            }
            measurementInputs[name] = input
            content.addView(input, LinearLayout.LayoutParams(-1, -2))
        }
        text("إعدادات الراحة وبدل الخياطة", 18f, true)
        text("الراحة (سهولة اللبس) تضاف إلى محيط الجسم، أما بدل الخياطة فيضاف عند حواف الباترون. لا تخلط بينهما.")
        val comfortInput = numberInput("راحة التصميم بالسنتيمتر", "12")
        val seamInput = numberInput("بدل الخياطة لكل حافة بالسنتيمتر", "1")
        val modelNames = models.map { it.name }.toTypedArray()
        var chosen = selectedGarment ?: models.first()
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, modelNames)
        spinner.setSelection(models.indexOfFirst { it.id == chosen.id }.coerceAtLeast(0))
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) { chosen = models[position] }
        }
        content.addView(spinner)
        button("حفظ المقاسات وحساب العرض التقريبي") {
            var valid = true
            measurementInputs.forEach { (key, field) ->
                val v = field.text.toString().replace(',', '.').toDoubleOrNull()
                if (v == null || v <= 0 || v > 300) valid = false else measurements[key] = v
            }
            val comfort = comfortInput.text.toString().replace(',', '.').toDoubleOrNull()
            val seam = seamInput.text.toString().replace(',', '.').toDoubleOrNull()
            if (!valid || comfort == null || comfort < 0 || comfort > 50 || seam == null || seam < 0 || seam > 5) {
                AlertDialog.Builder(this).setTitle("تحقق من البيانات").setMessage("أدخل قياسات موجبة ومعقولة، وراحة من 0 إلى 50 سم، وبدل خياطة من 0 إلى 5 سم.").setPositiveButton("حسنًا", null).show()
                return@button
            }
            saveMeasurements()
            selectedGarment = chosen
            val chest = measurements["محيط الصدر"] ?: 0.0
            val quarter = (chest + comfort) / 4.0 + seam
            val hip = ((measurements["محيط الأرداف"] ?: chest) + comfort) / 4.0 + seam
            val waist = ((measurements["محيط الخصر"] ?: chest) + comfort) / 4.0 + seam
            val msg = "الموديل: ${chosen.name}\nمحيط الصدر: ${fmt(chest)} سم\nعرض ربع الصدر التقديري: ${fmt(quarter)} سم\nعرض ربع الخصر التقديري: ${fmt(waist)} سم\nعرض ربع الأرداف التقديري: ${fmt(hip)} سم\n\nالصيغ:\n(المحيط + الراحة) ÷ 4 + بدل الحافة.\nالراحة: ${fmt(comfort)} سم للمحيط الكامل.\nبدل الخياطة: ${fmt(seam)} سم لكل حافة.\n\nهذه قياسات مساعدة وليست باترونًا نهائيًا. لا تشمل البنسات أو شكل القصّة أو اتجاه النسيج أو تفاصيل الياقة والكم. راجع بطاقة الموديل ونفذ بروفة."
            AlertDialog.Builder(this).setTitle("نتيجة الحساب").setMessage(msg)
                .setPositiveButton("عرض الباترون") { _, _ -> showGarment(chosen) }
                .setNeutralButton("نسخ النتيجة") { _, _ ->
                    (getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager)
                        .setPrimaryClip(android.content.ClipData.newPlainText("حساب الباترون", msg))
                    Toast.makeText(this, "تم نسخ النتيجة", Toast.LENGTH_SHORT).show()
                }.show()
        }
        button("استعادة القيم التجريبية") {
            measurements["محيط الصدر"] = 92.0; measurements["محيط الخصر"] = 76.0
            measurements["محيط الأرداف"] = 100.0; measurements["عرض الكتفين"] = 38.0
            measurements["طول الظهر"] = 40.0; measurements["طول القطعة"] = 100.0
            measurements["طول الكم"] = 58.0; measurements["محيط أعلى الذراع"] = 30.0
            measurements["محيط الرقبة"] = 36.0
            saveMeasurements(); showMeasurements()
        }
    }

    private fun numberInput(label: String, default: String): EditText {
        text(label, 14f, true)
        val e = EditText(this).apply {
            setSingleLine(true); setText(default)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setBackgroundColor(Color.WHITE)
        }
        content.addView(e, LinearLayout.LayoutParams(-1, -2))
        return e
    }

    private fun showGuide() {
        currentScreen = "guide"
        shell("دليل القص والتركيب", "خطوات أساسية لكل الموديلات")
        val sections = listOf(
            "1. تجهيز القماش" to "اقرأ تعليمات العناية، واغسل أو عالج القماش إن لزم ثم جففه واكوه.",
            "2. تحديد الوجه وخط النسيج" to "ميّز الوجه والظهر، وافحص النقشة والوبر والتمدد. ضع سهم خط النسيج موازيًا لحافة القماش حسب الباترون.",
            "3. أخذ المقاسات" to "قِس الجسم فوق ملابس خفيفة وسجّل الصدر والخصر والأرداف والكتف والطول والكم. أعد القياس للتحقق.",
            "4. الراحة وبدل الخياطة" to "الراحة مقدار يضاف لمحيط الجسم لحرية الحركة. بدل الخياطة مساحة بين خط الخياطة وحافة القص. يختلف الاثنان حسب التصميم والقماش.",
            "5. رسم الباترون" to "اكتب اسم القطعة وعدد القصات، وحدد خط الطي وخط النسيج وعلامات المطابقة. الباترون يحتاج تفاصيل خاصة بكل تصميم.",
            "6. ترتيب القطع" to "ضع القطع على القماش قبل القص، وراجع اتجاه النقشة وتطابق القطع واستهلاك القماش.",
            "7. القص التجريبي" to "قص نموذجًا أوليًا من قماش مشابه ورخيص. لا تقص القماش الغالي قبل التأكد من الملاءمة.",
            "8. التثبيت والبروفة" to "سرّج القطع مؤقتًا وجربها؛ تحقق من حركة الذراع والجلوس وتوازن الطول وموضع الخصر.",
            "9. ترتيب الخياطة" to "عادةً تُنفذ البنسات أو القصّات أولًا، ثم الكتفين والجوانب، ثم الياقة والأكمام والسحاب والبطانة وفق الموديل.",
            "10. التشطيب" to "نظف الحواف، واكْوِ الوصلات بحرارة تناسب القماش، وافحص الأزرار والسحاب والتوازن النهائي."
        )
        sections.forEach { (title, desc) -> card(title, desc) { AlertDialog.Builder(this).setTitle(title).setMessage(desc).setPositiveButton("حسنًا", null).show() } }
        text("سلامة: استخدم سطح قص ثابتًا، وأبعد المقص والإبر عن الأطفال، ولا تقص باتجاه جسمك.")
    }

    private fun saveMeasurements() {
        val obj = JSONObject()
        measurements.forEach { (k, v) -> obj.put(k, v) }
        getSharedPreferences("khiyati", MODE_PRIVATE).edit().putString("measurements", obj.toString()).apply()
    }

    private fun loadMeasurements() {
        val raw = getSharedPreferences("khiyati", MODE_PRIVATE).getString("measurements", null) ?: return
        try {
            val obj = JSONObject(raw)
            measurements.keys.toList().forEach { key -> if (obj.has(key)) measurements[key] = obj.optDouble(key, measurements[key] ?: 0.0) }
        } catch (_: Exception) { }
    }

    private fun fmt(v: Double) = String.format(java.util.Locale.US, "%.1f", v)
}

private class PatternSketch(context: Context, private val type: String) : View(context) {
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(105, 55, 88); style = Paint.Style.STROKE; strokeWidth = 3f
    }
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(54, 42, 50); textSize = 13f; textAlign = Paint.Align.CENTER
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(255, 255, 255))
        val w = width.toFloat(); val h = height.toFloat()
        val isCoat = type.startsWith("coat")
        val isRobe = type.startsWith("robe") || type.startsWith("abaya") || type.startsWith("yemen")
        val top = h * .13f; val bottom = h * .78f
        val left = w * .20f; val right = w * .49f
        fill.color = Color.rgb(247, 231, 239); fill.style = Paint.Style.FILL
        val p = android.graphics.Path()
        p.moveTo(left, top + 28); p.lineTo(left + 18, top); p.lineTo(right - 18, top)
        p.lineTo(right, top + 28); p.lineTo(right - 8, bottom)
        p.lineTo(left + 8, bottom); p.close()
        canvas.drawPath(p, fill); canvas.drawPath(p, line)
        val p2 = android.graphics.Path()
        p2.moveTo(w*.57f, top+28); p2.lineTo(w*.57f+18, top); p2.lineTo(w*.78f-18, top)
        p2.lineTo(w*.78f, top+28); p2.lineTo(w*.78f-8, bottom); p2.lineTo(w*.57f+8, bottom); p2.close()
        canvas.drawPath(p2, fill); canvas.drawPath(p2, line)
        if (isCoat) {
            canvas.drawLine(w*.345f, top+4, w*.345f, bottom-4, line)
            canvas.drawCircle(w*.365f, top+65, 3.5f, line)
            canvas.drawCircle(w*.365f, top+88, 3.5f, line)
        } else if (isRobe) {
            canvas.drawLine(w*.345f, top+4, w*.345f, bottom-4, line)
        } else {
            canvas.drawLine(w*.345f, top+5, w*.345f, top+60, line)
            canvas.drawLine(w*.345f-10, top+60, w*.345f+10, top+60, line)
        }
        canvas.drawText("أمام / تخطيطي", w*.345f, h*.88f, label)
        canvas.drawText("خلف / تخطيطي", w*.675f, h*.88f, label)
        canvas.drawText("رسم تعليمي غير مقياسي", w*.5f, h*.96f, label)
    }
}
