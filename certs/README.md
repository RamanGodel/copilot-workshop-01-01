# Corporate Root CA (optional)

If your network/VPN performs TLS inspection (MITM) and re-signs HTTPS certificates, Java inside Docker may fail with:

`PKIX path building failed` / `unable to find valid certification path`

To fix it:

1. Export your corporate root CA certificate as PEM (Base-64) format.
2. Save it to:

   `certs/corp-root-ca.crt`

3. Set `CORPORATE_CA_ENABLED=true` in your `.env` (or shell env) and restart Docker Compose.

> Do **not** commit `corp-root-ca.crt` to git.

