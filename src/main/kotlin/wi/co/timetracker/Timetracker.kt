package wi.co.timetracker

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import tornadofx.App
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.launch
import wi.co.timetracker.view.MainView
import kotlin.reflect.KClass


class Timetracker : App(MainView::class) {
    init {
        val springContext = AnnotationConfigApplicationContext(Timetracker::class.java.`package`.name)
        FX.dicontainer = object : DIContainer {
            override fun <T : Any> getInstance(type: KClass<T>): T = springContext.getBean(type.java)
        }
    }
}

fun main(args: Array<String>) = launch<Timetracker>(args)
