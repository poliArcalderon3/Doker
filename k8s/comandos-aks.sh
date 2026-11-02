# =====================================================================
#  DESPLIEGUE DEL GESTOR DE PEDIDOS EN AZURE KUBERNETES SERVICE (AKS)
#  Ejecutar BLOQUE POR BLOQUE (no todo de una vez).
#    - Bloques "CLOUD SHELL": en https://shell.azure.com (modo Bash)
#    - Bloque  "TU PC":       en PowerShell, con Docker Desktop abierto
# =====================================================================

# ---------- PASO 0 (ANTES de grabar) · CLOUD SHELL ---------------------
az account show -o table                 # debe decir "Azure for Students"

az provider register --namespace Microsoft.ContainerService
az provider register --namespace Microsoft.ContainerRegistry

# Nombre del registro: unico en todo Azure, solo minusculas y numeros
export RG=rg-pedidos
export LOC=eastus2
export AKS=aks-pedidos
export ACR=acrpedidosarc2026             # cambiar si dice que ya existe

az group create -n $RG -l $LOC
az acr create -g $RG -n $ACR --sku Basic

# Cluster AKS nivel Free: 2 nodos pequenos, autoescalado hasta 3 (tarda 5-10 min)
az aks create -g $RG -n $AKS \
  --tier free \
  --node-count 2 --node-vm-size Standard_B2s \
  --enable-cluster-autoscaler --min-count 2 --max-count 3 \
  --attach-acr $ACR \
  --generate-ssh-keys

# ---------- PASO 0b: construir y subir las imagenes ---------------------
# Opcion A (CLOUD SHELL): ACR construye las imagenes en la nube.
#   Si responde "TasksOperationsNotAllowed", usar la opcion B.
git clone https://github.com/poliArcalderon3/Doker.git
cd Doker
az acr build -r $ACR -t pedidos-mysql:1.0 ./db
az acr build -r $ACR -t pedidos-app:1.0 .
sed -i 's|<h1>Sistema de Pedidos</h1>|<h1>Sistema de Pedidos v2 - AKS</h1>|' src/main/resources/static/index.html
az acr build -r $ACR -t pedidos-app:2.0 .
git checkout src/main/resources/static/index.html

# Opcion B (TU PC, PowerShell, en D:\Proyectos Git\Doker\gestorPedidos):
#   1) En Cloud Shell habilitar usuario admin y ver la clave:
#        az acr update -n $ACR --admin-enabled true
#        az acr credential show -n $ACR
#   2) En PowerShell:
#        $ACR = "acrpedidosarc2026"
#        docker login "$ACR.azurecr.io"        # usuario y password del paso 1
#        docker build -t "$ACR.azurecr.io/pedidos-mysql:1.0" ./db
#        docker push "$ACR.azurecr.io/pedidos-mysql:1.0"
#        docker build -t "$ACR.azurecr.io/pedidos-app:1.0" .
#        docker push "$ACR.azurecr.io/pedidos-app:1.0"
#        (Get-Content src\main\resources\static\index.html -Raw) -replace '<h1>Sistema de Pedidos</h1>','<h1>Sistema de Pedidos v2 - AKS</h1>' | Set-Content src\main\resources\static\index.html -NoNewline
#        docker build -t "$ACR.azurecr.io/pedidos-app:2.0" .
#        docker push "$ACR.azurecr.io/pedidos-app:2.0"
#        git checkout src/main/resources/static/index.html

az acr repository list -n $ACR -o table   # deben aparecer pedidos-mysql y pedidos-app

# Para ahorrar credito entre la practica y la grabacion:
#   az aks stop  -g $RG -n $AKS
#   az aks start -g $RG -n $AKS

# =====================================================================
#  DESDE AQUI SE GRABA · CLOUD SHELL
#  (si es una sesion nueva: repetir los export del paso 0 y
#   "cd ~/Doker")
# =====================================================================

# ---------- PASO 1: conectarse al cluster ------------------------------
az aks get-credentials -g $RG -n $AKS
kubectl get nodes -o wide
kubectl get namespaces

# ---------- PASO 2: desplegar la base de datos -------------------------
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-secret.yaml
sed "s|IMAGEN_MYSQL|$ACR.azurecr.io/pedidos-mysql:1.0|" k8s/02-mysql.yaml | kubectl apply -f -
kubectl get pvc,pods -n pedidos -w        # esperar mysql 1/1 Running, Ctrl+C

# ---------- PASO 3: desplegar la app Spring Boot -----------------------
sed "s|IMAGEN_APP|$ACR.azurecr.io/pedidos-app:1.0|" k8s/03-app.yaml | kubectl apply -f -
kubectl get pods -n pedidos -o wide -w    # esperar 3 pods 1/1 Running, Ctrl+C
kubectl get svc app-pedidos -n pedidos -w # esperar EXTERNAL-IP, Ctrl+C
export IP=$(kubectl get svc app-pedidos -n pedidos -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
echo "http://$IP"                         # abrir en el navegador: registrar cliente y pedido

curl -s http://$IP/api/clientes
curl -s http://$IP/api/pedidos/resumen
curl -s http://$IP/actuator/health

# Balanceo de carga: recargar consulta.html mientras corre
kubectl logs -n pedidos -l app=app-pedidos --prefix -f --tail=0   # Ctrl+C

# Portal: Servicios de Kubernetes > aks-pedidos > Cargas de trabajo / Servicios / Almacenamiento
# Portal: grupo de recursos MC_rg-pedidos_aks-pedidos_eastus2 (VMSS, Load Balancer, IP publica, disco)

# ---------- PASO 4: escalado manual ------------------------------------
kubectl scale deployment app-pedidos -n pedidos --replicas=5
kubectl get pods -n pedidos -o wide -w
kubectl scale deployment app-pedidos -n pedidos --replicas=3

# ---------- PASO 5: autorreparacion y persistencia ---------------------
kubectl delete pod -n pedidos $(kubectl get pods -n pedidos -l app=app-pedidos -o name | head -1 | cut -d/ -f2)
kubectl get pods -n pedidos
kubectl delete pod -n pedidos -l app=mysql
kubectl get pods -n pedidos -w            # esperar mysql Running, Ctrl+C
curl -s http://$IP/api/clientes           # los clientes registrados siguen ahi

# ---------- PASO 6: actualizacion continua y rollback -------------------
kubectl set image deployment/app-pedidos app=$ACR.azurecr.io/pedidos-app:2.0 -n pedidos
kubectl rollout status deployment/app-pedidos -n pedidos
# Recargar el navegador: el titulo ahora dice "v2 - AKS"
kubectl rollout history deployment/app-pedidos -n pedidos
kubectl rollout undo deployment/app-pedidos -n pedidos

# ---------- PASO 7: autoescalado (HPA) ---------------------------------
kubectl apply -f k8s/04-hpa.yaml
kubectl run carga -n pedidos --image=busybox:1.36 --restart=Never -- \
  /bin/sh -c "while true; do wget -q -O- http://app-pedidos/api/pedidos/resumen; done"
kubectl get hpa -n pedidos -w             # 1-3 min: sube CPU y replicas
kubectl delete pod carga -n pedidos

# ---------- PASO 8: observabilidad -------------------------------------
kubectl describe deployment app-pedidos -n pedidos
kubectl top nodes
kubectl top pods -n pedidos

# ---------- PASO 9: LIMPIEZA (evita gastar el credito) ------------------
az group delete -n $RG --yes --no-wait    # borra cluster, registro, discos e IP

# ---------- Solucion de problemas --------------------------------------
# "RequestDisallowedByAzure" o region no permitida: Azure for Students limita
#   las regiones. Probar LOC=centralus, southcentralus o westus2.
# Error de cuota de vCPU: usar --node-count 1 --min-count 1, o
#   --node-vm-size Standard_B2s_v2 / Standard_D2s_v3.
# Pods en ImagePullBackOff: az aks check-acr -g $RG -n $AKS --acr $ACR.azurecr.io
# Pods en Pending: kubectl describe pod <nombre> -n pedidos  (falta CPU/memoria;
#   el autoescalador del cluster agrega un nodo en unos minutos)
# App reiniciando: kubectl logs -n pedidos deployment/app-pedidos
# MySQL no arranca: kubectl logs -n pedidos deployment/mysql
