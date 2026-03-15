# Análise e melhorias – app/src

Resumo da análise dos ficheiros em `app/src` e melhorias aplicadas ou recomendadas.

---

## Melhorias já aplicadas

### 1. **Bug: contagem de likes no HomeCommentCard**
- **Ficheiro:** `CommunityHomeScreen.kt`
- **Problema:** Na linha do “gosto”, usava-se `item.likes` em vez do valor do cache (`likes`), pelo que ao voltar do ecrã do número a contagem não atualizava.
- **Correção:** Passou a usar a variável `likes` (que vem de `LikeCache.getLikes()`).

### 2. **PhoneStateReceiver: remoção de Thread.sleep**
- **Ficheiro:** `PhoneStateReceiver.kt`
- **Problema:** `getLastIncomingNumberFromCallLog()` usava `Thread.sleep(300)` no contexto do `BroadcastReceiver`, bloqueando o processo.
- **Correção:** Quando o intent não traz número, a resolução do número via CallLog passou a ser feita em background com `goAsync()`, um executor e `Handler` para voltar ao main thread; o receiver termina sem bloquear.

### 3. **OverlayPreferences: constante do enum**
- **Ficheiro:** `OverlayPreferences.kt`
- **Problema:** `DEFAULT_STYLE = 1` era um “magic number”.
- **Correção:** Passou a usar `OverlayStyle.PILL.id` como valor por omissão.

### 4. **LigaramLogo: acessibilidade**
- **Ficheiro:** `LigaramLogo.kt`
- **Problema:** `contentDescription = null` no `Image` do logo.
- **Correção:** Definido `contentDescription = "Logo ligaram.me"`.

### 5. **Color.kt: limpeza**
- **Ficheiro:** `ui/theme/Color.kt`
- **Problema:** Cores de template (Purple80, Pink40, etc.) não utilizadas.
- **Correção:** Remoção das constantes não usadas e comentário a explicar o propósito do ficheiro.

---

## Recomendações (não aplicadas)

### Segurança

- **Tokens de API em código** (`ApiClient.kt`, `CommunityApi.kt`):  
  `API_TOKEN` / `TOKEN` estão hardcoded. Em produção, usar `BuildConfig` (e.g. `BuildConfig.API_TOKEN`) ou outro mecanismo de secrets (variáveis de ambiente, keystore, etc.) e nunca commitar o valor real.

### Logs

- **Logs em produção:** Vários `Log.d` / `Log.e` expõem URLs e números.  
  Recomendação: usar uma biblioteca como Timber com nível de log condicionado a `BuildConfig.DEBUG`, ou remover/redactar em release.

### Permissões (Compose)

- **Accompanist Permissions** em `Screens.kt`:  
  O uso de `com.google.accompanist.permissions` está deprecated. A abordagem recomendada é `androidx.activity.compose.rememberMultiplePermissionsState()` (ou o equivalente da API estável de permissões no Compose).

### Polling de permissões

- **HomeScreen e PermOverlayScreen:**  
  `while (true) { delay(500); ... }` e `while (!canDraw.value) { delay(500); ... }` fazem polling contínuo.  
  Melhor: reavaliar permissões apenas quando a activity regressa ao foreground (ex.: `LaunchedEffect(Unit)` + `Lifecycle` / `ActivityResultRegistry`) em vez de um loop infinito.

### Strings e i18n

- **Textos em português nos composables:**  
  Muitas strings estão hardcoded nos ficheiros Kotlin. Para manutenção e i18n, convém movê-las para `res/values/strings.xml` (e variantes por idioma se necessário).

### Acessibilidade

- **Ícones com `contentDescription = null`:**  
  Vários `Icon(..., contentDescription = null)` em `Screens.kt` e noutros ecrãs. Para leitores de ecrã, é preferível usar descrições breves onde fizer sentido (e `null` apenas quando o ícone for puramente decorativo).

### Tema

- **Typography** em `Type.kt`:  
  A tipografia está definida mas não é passada a `MaterialTheme` em `Theme.kt`. Para consistência, pode usar-se `MaterialTheme(typography = Typography, ...)`.

### Ícone da notificação

- **CallMonitorService:**  
  Usa `android.R.drawable.ic_menu_call`. Para identidade da app, é preferível um ícone próprio (e.g. `R.drawable.ic_ligaram_logo` ou ícone de notificação dedicado).

### Overlay / estado

- **OverlayActivity:**  
  O estado do overlay está em `mutableStateOf` na Activity. Para testes e separação de responsabilidades, pode ser movido para um ViewModel ou para um estado imutável atualizado a partir do Intent.

---

## Estrutura analisada

- **app/src/main/java/me/ligaram/app/**  
  - `MainActivity.kt`, `data/*` (ApiClient, CallInfo, CommunityApi, CommunityModels, DeviceHeaders, LikeCache, OverlayPreferences), `service/*` (BootReceiver, CallMonitorService, PhoneStateReceiver), `ui/screens/*` (Screens, OverlayActivity, CommunityHomeScreen, CommunityNumberScreen, AddCommentScreen, OverlayStyleScreen, LigaramLogo), `ui/theme/*` (Theme, Color, Type).
- **app/src/main/res/**  
  - values (strings, colors, themes), drawable, xml, etc.
- **AndroidManifest.xml**

Se quiseres, posso detalhar ou implementar alguma das recomendações em concreto (por exemplo: migração de permissões, extração de strings ou configuração de tokens via BuildConfig).
