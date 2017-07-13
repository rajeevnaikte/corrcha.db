# corrcha.db
Java DB adapter to connect and perform CRUD on Cassandra DB with feature to add audit entries into separate audit table simply with annotation @Audited on your entity class

# Sample
DBFacade dbFacade = DBFactory.getDataStaxAdapter();<br/>
dbFacade.connect("127.0.0.1", null, "audit", null, null);<br/>
dbFacade.parseEntity(EntityPojo.class);<br/>
EntityPojo entity = new EntityPojo();<br/>
dbFacade.save(entity);<br/>
dbFacade.disconnect();<br/>
<br/>
//Sample EntityPojo with DataStax Table annotaiton and Audited annotation<br/>
@Table(table="entity", keySpace="demo")<br/>
@Audited<br/>
public class EntityPojo {<br/>
&nbsp;&nbsp;public String name;<br/>
<br/>
&nbsp;&nbsp;@NotAudited<br/>
&nbsp;&nbsp;public String creditCardNumber;<br/>
}<br/>
