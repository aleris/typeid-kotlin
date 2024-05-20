package earth.adi.typeid.testentities

import earth.adi.typeid.Id

data class Organization(val id: OrganizationId)

typealias OrganizationId = Id<out Organization>
