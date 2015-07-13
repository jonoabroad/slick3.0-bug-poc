import slick.driver.PostgresDriver.api._

case class User(
  id:    Option[Int],
  email: String
)

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Int]("email")
  def email = column[String]("email")

  def * = (id.?, email) <> ((User.apply _).tupled, User.unapply)
}

case class RoleUser(
  roleId: Int,
  userId: Int
)

class RoleUserTable(tag: Tag) extends Table[RoleUser](tag, "roles_users") {
  def roleId = column[Int]("role_id")
  def userId = column[Int]("user_id")

  def * = (roleId, userId) <> ((RoleUser.apply _).tupled, RoleUser.unapply)
}

case class Role(
  id:   Option[Int],
  name: String
)

class RoleTable(tag: Tag) extends Table[Role](tag, "roles") {
  def id = column[Int]("id")
  def name = column[String]("name")

  def * = (id.?, name) <> ((Role.apply _).tupled, Role.unapply)
}

object Main extends App {
  val db = Database.forURL(url = "", prop = Map.empty[String, String])

  val userTable = TableQuery[UserTable]
  val roleTable = TableQuery[RoleTable]
  val roleUserTable = TableQuery[RoleUserTable]

  val id = 1

  // What I expect in production env and models:
  //   select users.*, array_agg_custom(roles.rights) as rights
  //   from users
  //   left join roles_users on roles_users.user_id = users.id
  //   left join roles on roles_users.role_id = roles.id
  //   group by users.id

  db.run {
    (for {
      t <- userTable
        .filter(_.id === id)
        .take(1)
        .joinLeft(roleUserTable).on(_.id === _.userId)
        .joinLeft(roleTable.map { t =>
          (t.id, t.name) // arrayAggCustom(t.rights)
        }).on(_._2.map(_.roleId) === _._1)
        .groupBy(g => (g._1._1, g._2.map(_._2)))
    } yield t._1).result.headOption
  }
}
