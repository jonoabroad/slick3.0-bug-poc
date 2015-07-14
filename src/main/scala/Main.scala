import slick.driver.PostgresDriver.api._

case class PK[A](value: Long) extends AnyVal with MappedTo[Long]

case class User(email: String,id:Option[PK[UserTable]] = None)

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def id    = column[PK[UserTable]]("id")
  def email = column[String]("email")

  def * = (email,id.?) <> ((User.apply _).tupled, User.unapply)
}

case class RoleUser(roleId: PK[RoleTable],userId: PK[UserTable])

class RoleUserTable(tag: Tag) extends Table[RoleUser](tag, "roles_users") {
  def roleId = column[PK[RoleTable]]("role_id")
  def userId = column[PK[UserTable]]("user_id")

  def * = (roleId, userId) <> ((RoleUser.apply _).tupled, RoleUser.unapply)
}

case class Role(name: String,id:   Option[PK[RoleTable]] = None)

class RoleTable(tag: Tag) extends Table[Role](tag, "roles") {
  def id = column[PK[RoleTable]]("id")
  def name = column[String]("name")

  def * = (name,id.?) <> ((Role.apply _).tupled, Role.unapply)
}

object Main  {

    val db            = Database.forURL(url = "", prop = Map.empty[String, String])
    val userTable     = TableQuery[UserTable]
    val roleTable     = TableQuery[RoleTable]
    val roleUserTable = TableQuery[RoleUserTable]

  def main(args:Array[String]):Unit = {


  val id = PK[UserTable](1)
  
  val query =  userTable
    .filter(_.id === id)
    .take(1)    
    .joinLeft(roleUserTable).on(_.id === _.userId)
    .joinLeft(roleTable).on { case ((ut,orut),rt) => rt.id === orut.map(_.roleId) }
    .groupBy{case ((ut,orut),ort) => (ut.id,ort.map(_.name))}
    .map { case (k, g) => k }

    println(query.result.statements.mkString)
    val action = query.result
    db.run { action.headOption }

  }

}
