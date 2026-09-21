Para um sistema bancário real, eu não manteria o JWT manual atual como solução final. O caminho mais robusto é usar um provedor de identidade compatível com OAuth 2.1/OpenID Connect — por exemplo Keycloak gerenciado por vocês, Auth0, Okta, Azure Entra ID ou Cognito — e transformar esta API em um OAuth2 Resource Server.

A arquitetura recomendada seria:

```text
Web / Mobile
   │
   ├─ Authorization Code + PKCE ──> Identity Provider
   │                                  ├─ senha / passkey / MFA
   │                                  ├─ recuperação de conta
   │                                  ├─ rate limit e detecção de fraude
   │                                  └─ emissão e rotação de tokens
   │
   └─ Bearer access token ────────> Banking API
                                      ├─ valida assinatura via JWKS
                                      ├─ valida issuer, audience, exp, nbf
                                      ├─ aplica scopes/roles
                                      └─ valida posse da conta no banco
```

O ponto principal: a API não cria nem assina tokens. Ela só valida tokens emitidos pelo IdP por chaves públicas publicadas em JWKS. Com Spring Security isso normalmente vira uma configuração `issuer-uri`; o framework descobre as chaves, valida assinatura, expiração e emissor, e acompanha a rotação de chaves do provedor. [Spring Security Resource Server JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

Para este projeto, eu recomendaria:

- Access token JWT assinado de forma assimétrica (`RS256` ou `ES256`), com vida curta — tipicamente 5 a 10 minutos.
- Claims mínimas: `sub`, `iss`, `aud`, `exp`, `nbf`, `jti`, `scope`/roles. Não colocar dados financeiros nem `accountId` como fonte de autorização no token.
- Validar sempre `issuer` e `audience`, além da assinatura e validade temporal. Isso impede que um token de outro sistema seja aceito pela API.
- Manter a checagem atual de propriedade da conta no banco: `subject` do token → usuário → conta. Autenticação confirma “quem é”; autorização confirma “pode operar esta conta”.
- Refresh token com rotação e detecção de reutilização. A RFC 9700 exige rotação ou vínculo criptográfico ao cliente para refresh tokens de clientes públicos. [RFC 9700](https://www.rfc-editor.org/info/rfc9700/)
- Para navegador, preferir BFF com cookie `HttpOnly`, `Secure` e `SameSite`, em vez de guardar token no `localStorage`, que pode ser exposto por XSS. [OWASP Session Management](https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html)
- MFA obrigatório para ações sensíveis. Para máxima resistência a phishing, usar passkeys/WebAuthn/FIDO2; TOTP é um bom segundo caminho, SMS deve ser evitado como fator principal. [OWASP MFA](https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html)
- Step-up authentication para saque, alteração de dados, criação de favorecido ou valores altos: exigir uma nova confirmação MFA e, idealmente, vincular a confirmação aos detalhes da transação.
- Rate limit por IP e por usuário no login, resposta genérica para credenciais inválidas, auditoria de logins, alertas de dispositivo/localização incomuns e bloqueio progressivo contra credential stuffing.
- Para integrações máquina-a-máquina: OAuth Client Credentials com scopes mínimos; para maior proteção contra roubo de token, DPoP ou mTLS.
- Se a aplicação ainda armazenar senha localmente, preferir Argon2id; BCrypt é aceitável, mas Argon2id é a recomendação moderna. [OWASP Password Storage](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)

Minha escolha para este demo evoluiria assim:

1. Keycloak como IdP local em Docker Compose.
2. Spring Security OAuth2 Resource Server nesta API.
3. Remover `JwtTokenService`, `JwtAuthenticationFilter`, login e hash de senha próprios.
4. Mapear `sub` do IdP para `app_user.external_subject`.
5. Preservar `AccountAccess` para validar a titularidade da conta.
6. Adicionar scopes como `banking.read`, `banking.transfer` e `banking.admin`.
7. Exigir MFA/step-up para saques e operações de maior risco.

Isso separa a parte mais perigosa — identidade, credenciais, sessão, recuperação, MFA e rotação de chaves — da regra bancária, que continua focada em autorização e operações financeiras.