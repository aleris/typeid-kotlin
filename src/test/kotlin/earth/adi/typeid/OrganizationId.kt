package earth.adi.typeid

data class Organization(val id: OrganizationId)

typealias OrganizationId = Id<out Organization>
