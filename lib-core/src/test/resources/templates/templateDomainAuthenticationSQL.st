# Silverpeas default driver authentication
# ----------------------------------------

# Fallback type : could be one of the following values : none, ifNotRejected, always
fallbackType         = none
allowPasswordChange  = $allowPasswordChange$

# Authentication servers
# Available types are :
#       org.silverpeas.authentication.AuthenticationNT
#       org.silverpeas.authentication.AuthenticationSQL
#       org.silverpeas.authentication.AuthenticationLDAP
autServersCount = 1
autServer0.type    								= org.silverpeas.authentication.AuthenticationSQL
autServer0.enabled 								= true
autServer0.SQLJDBCUrl 							= $SQLJDBCUrl$
autServer0.SQLAccessLogin 						= $SQLAccessLogin$
autServer0.SQLAccessPasswd 						= $SQLAccessPasswd$
autServer0.SQLDriverClass 						= $SQLDriverClass$
autServer0.SQLUserTableName 					= $SQLUserTableName$
autServer0.SQLUserLoginColumnName 				= login
autServer0.SQLUserPasswordColumnName 			= password
autServer0.SQLUserPasswordAvailableColumnName 	= passwordValid

