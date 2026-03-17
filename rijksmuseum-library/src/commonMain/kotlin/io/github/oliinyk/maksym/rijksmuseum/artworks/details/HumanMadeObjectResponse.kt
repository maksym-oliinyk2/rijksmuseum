package io.github.oliinyk.maksym.rijksmuseum.artworks.details

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class HumanMadeObjectResponse(
    @SerialName("@context")
    val context: String,
    val id: String,
    val type: String,//expected HumanMadeObject
    @SerialName("produced_by")
    val producedBy: Production? = null,
    @SerialName("subject_of")
    val subjectOf: List<LinguisticObject> = emptyList(),
    @SerialName("assigned_by")
    val assignedBy: List<AttributeAssignment> = emptyList(),
    @SerialName("identified_by")
    val identifiedBy: List<Identification> = emptyList(),
    @SerialName("classified_as")
    val classifiedAs: List<TypeObject> = emptyList(),
    val dimension: List<Dimension> = emptyList(),
    @SerialName("made_of")
    val madeOf: List<Material> = emptyList(),
    val shows: List<VisualItemBrief> = emptyList(),
    @SerialName("referred_to_by")
    val referredToBy: List<LinguisticObject> = emptyList(),
    val equivalent: List<Equivalent> = emptyList(),
    @SerialName("member_of")
    val memberOf: List<SetObject> = emptyList()
)

@Serializable
public data class Production(
    val type: String,
    val technique: List<TypeObject> = emptyList(),
    val timespan: TimeSpan? = null,
    /* @SerialName("referred_to_by")
     val referredToBy: List<LinguisticObject> = emptyList(),*/
    val part: List<ProductionPart> = emptyList()
)

@Serializable
public data class ProductionPart(
    val type: String,
    @SerialName("assigned_by")
    val assignedBy: List<AttributeAssignment> = emptyList(),
    @SerialName("identified_by")
    val identifiedBy: List<Identification> = emptyList(),
    @SerialName("classified_as")
    val classifiedAs: List<TypeObjectOrString> = emptyList(),
    val technique: List<TypeObject> = emptyList(),
    @SerialName("referred_to_by")
    val referredToBy: List<LinguisticObject> = emptyList()
)

@Serializable
public data class TimeSpan(
    val type: String,
    @SerialName("identified_by")
    val identifiedBy: List<Identification> = emptyList(),
    @SerialName("begin_of_the_begin")
    val beginOfTheBegin: String? = null,
    @SerialName("end_of_the_end")
    val endOfTheEnd: String? = null
)

@Serializable
public data class LinguisticObject(
    val id: String? = null,
    val type: String,
    val content: String? = null,
    /*  @SerialName("classified_as")
      val classifiedAs: List<TypeObjectOrString> = emptyList(),*/
    val language: List<TypeObject> = emptyList(),
    /* @SerialName("digitally_carried_by")
     val digitallyCarriedBy: List<DigitalObject> = emptyList(),*/
    @SerialName("subject_to")
    val subjectTo: List<Right> = emptyList(),
    @SerialName("part_of")
    val partOf: List<LinguisticObject> = emptyList(),
    @SerialName("identified_by")
    val identifiedBy: List<Identification> = emptyList(),
    @SerialName("created_by")
    val createdBy: Creation? = null
)

@Serializable
public data class Creation(
    val type: String,
    val timespan: TimeSpan? = null
)

@Serializable
public data class Right(
    val type: String,
    @SerialName("classified_as")
    val classifiedAs: List<TypeObject> = emptyList()
)

@Serializable
public data class AttributeAssignment(
    val type: String,
    val assigned: List<Assigned> = emptyList(),
    @SerialName("assigned_property")
    val assignedProperty: String? = null,
    @SerialName("motivated_by")
    val motivatedBy: List<LinguisticObject> = emptyList(),
    @SerialName("classified_as")
    val classifiedAs: List<TypeObject> = emptyList(),
    @SerialName("referred_to_by")
    val referredToBy: List<LinguisticObject> = emptyList()
)

@Serializable
public data class Assigned(
    val id: String? = null,
    val type: String? = null,
    // Sometimes assigned is just a string URL
    val value: String? = null
)

// This handles cases where classified_as can be an object or just a string URL
// However, the provided JSON seems to always use objects for classified_as, 
// EXCEPT in ProductionPart (line 252) where it's a string.
// We'll use a sealed class or just a generic class for now.
@Serializable
public data class TypeObject(
    val id: String? = null,
    val type: String? = null,
    //val notation: List<Notation> = emptyList(),
    val equivalent: List<String> = emptyList(),
    @SerialName("classified_as")
    val classifiedAs: List<TypeObject> = emptyList(),
    @SerialName("_label")
    val label: String? = null
)

// Workaround for mixed types in classified_as
@Serializable
public data class TypeObjectOrString(
    val id: String? = null,
    val type: String? = null,
    @SerialName("_label")
    val label: String? = null
)

@Serializable
public data class Identification(
    val type: String,// filter for Name
    val content: String? = null,
)

/*
@Serializable
public data class Notation(
    @SerialName("@language")
    val language: String,
    @SerialName("@value")
    val value: String
)
*/

@Serializable
public data class Dimension(
    val type: String,
    @SerialName("identified_by")
    val identifiedBy: List<Identification> = emptyList(),
    @SerialName("classified_as")
    val classifiedAs: List<TypeObject> = emptyList(),
    val value: String? = null,
    val unit: MeasurementUnit? = null
)

@Serializable
public data class MeasurementUnit(
    val id: String,
    val type: String
)

@Serializable
public data class Material(
    val id: String,
    val type: String,
    @SerialName("identified_by")
    val identifiedBy: List<Identification> = emptyList(),
    //val notation: List<Notation> = emptyList(),
    val equivalent: List<String> = emptyList()
)

@Serializable
public data class VisualItemBrief(
    val id: String,
    val type: String,
)

@Serializable
public data class Equivalent(
    val id: String,
    val type: String
)

@Serializable
public data class SetObject(
    val id: String,
    val type: String
)


////


