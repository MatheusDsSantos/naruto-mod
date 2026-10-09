# Desenha a textura 16x16 da kunai do Minato (três pontas), na diagonal como as espadas do Minecraft.
# Uso: powershell -File tools\gerar_textura_kunai.ps1
# Gera também kunai_preview.png (ampliada 16x) em tools\ para conferir o desenho.

Add-Type -AssemblyName System.Drawing

$cores = @{
	'k' = '#4a4f5a'  # aço escuro (lado de baixo da lâmina, guarda)
	's' = '#9aa3ad'  # aço
	'l' = '#dfe6ec'  # aço claro (brilho)
	'd' = '#1f2233'  # cabo escuro
	'w' = '#d9cfb4'  # faixa do cabo (onde fica a fórmula do Hiraishin)
	'r' = '#7d8590'  # argola
}

# Cada linha é uma fileira de pixels (y=0 em cima). '.' = transparente.
# A lâmina vai do canto inferior esquerdo (argola) até o superior direito (ponta).
$desenho = @(
	'...............l'
	'.............ls.'
	'............lsk.'
	'.......l...lsk..'
	'........s.lsk...'
	'........slsk....'
	'.........ks.....'
	'........kkssl...'
	'.......dd...s...'
	'......ww.....l..'
	'.....dd.........'
	'....ww..........'
	'...dd...........'
	'.r.d............'
	'r.r.............'
	'.r..............'
)

$raiz = Split-Path -Parent $PSScriptRoot
$saida = Join-Path $raiz 'src\main\resources\assets\narutomod\textures\item\kunai.png'
$preview = Join-Path $PSScriptRoot 'kunai_preview.png'

$bmp = New-Object System.Drawing.Bitmap 16, 16, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
for ($y = 0; $y -lt 16; $y++) {
	$linha = $desenho[$y]
	if ($linha.Length -ne 16) { throw "Linha $y tem $($linha.Length) pixels (precisa de 16)" }
	for ($x = 0; $x -lt 16; $x++) {
		$c = [string]$linha[$x]
		if ($c -eq '.') { $bmp.SetPixel($x, $y, [System.Drawing.Color]::Transparent) }
		else { $bmp.SetPixel($x, $y, [System.Drawing.ColorTranslator]::FromHtml($cores[$c])) }
	}
}
$bmp.Save($saida, [System.Drawing.Imaging.ImageFormat]::Png)

$big = New-Object System.Drawing.Bitmap 256, 256
$g = [System.Drawing.Graphics]::FromImage($big)
$g.Clear([System.Drawing.Color]::FromArgb(255, 139, 139, 139))
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
$g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
$g.DrawImage($bmp, 0, 0, 256, 256)
$big.Save($preview, [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose(); $big.Dispose(); $bmp.Dispose()

"Textura: $saida"
"Preview: $preview"
