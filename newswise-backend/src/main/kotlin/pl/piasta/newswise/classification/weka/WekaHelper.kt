package pl.piasta.newswise.classification.weka

import java.io.File
import pl.piasta.newswise.common.sevenZipSingleStream
import weka.classifiers.meta.FilteredClassifier
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instances
import weka.core.SerializationHelper

private const val PREDICTION_INSTANCE = "prediction"
private const val CLASS_ATTRIBUTE = "class-att"
private const val TEXT_ATTRIBUTE = "text-att"

object WekaHelper {
    fun readClassifier(model: File) = model.sevenZipSingleStream().use {
        SerializationHelper.read(it) as FilteredClassifier
    }

    fun createAttributes(classes: List<String>): ArrayList<Attribute> {
        val classAttribute = Attribute(CLASS_ATTRIBUTE, classes)
        val textAttribute = Attribute(TEXT_ATTRIBUTE, null as? List<String>?)
        return arrayListOf(classAttribute, textAttribute)
    }

    fun createPreditionInstance(text: String, attributes: ArrayList<Attribute>): DenseInstance {
        val dataset = Instances(PREDICTION_INSTANCE, attributes, 1).apply { setClassIndex(0) }
        return DenseInstance(attributes.size).apply {
            setDataset(dataset)
            setValue(attributes[1], text)
        }
    }
}
