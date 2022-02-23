SET user=Patient4
keytool -genkeypair -alias %user% -keyalg RSA -keysize 4096 -keystore %user%KeyStore -validity 365 -storetype JKS -storepass password
keytool -certreq -alias %user% -keystore %user%KeyStore -file %user%CsrReq.csr -storepass password
openssl  x509  -req -CA ca.pem -CAkey ca.key -in %user%CsrReq.csr -out %user%cert.pem -days 365 -sha256 -CAcreateserial
keytool -importcert -trustcacerts -keystore %user%KeyStore -storepass password -file ca.pem -alias CA
keytool -importcert -trustcacerts -keystore %user%KeyStore -storepass password -file %user%cert.pem -alias %user%
